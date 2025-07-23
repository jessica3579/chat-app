package com.example.liontalk.features.chatroom

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liontalk.data.local.entity.ChatMessageEntity
import com.example.liontalk.data.remote.dto.ChatMessageDto
import com.example.liontalk.data.remote.dto.TypingMessageDto
import com.example.liontalk.data.remote.mqtt.MqttClient
import com.example.liontalk.data.repository.ChatMessageRepository
import com.example.liontalk.data.repository.UserPreferenceRepository
import com.example.liontalk.model.ChatUser
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomViewModel(application: Application, private val roomId: Int) : ViewModel() {
    private val chatMessageRepository = ChatMessageRepository(application)

    //    val messages: LiveData<List<ChatMessageEntity>> = chatMessageRepository.getMessageForRoom(roomId)

    val messages: StateFlow<List<ChatMessageEntity>> =
        chatMessageRepository.getMessageForRoomFlow(roomId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val userPreferenceRepository = UserPreferenceRepository.getInstance()
    val me: ChatUser get() = userPreferenceRepository.requireMe()

    private val _event = MutableSharedFlow<ChatRoomEvent>()
    val event = _event.asSharedFlow()

    init {
        viewModelScope.launch {

            withContext(Dispatchers.IO) {
                MqttClient.connect()
            }

            withContext(Dispatchers.IO) {
                subscribeToMqttTopics()
            }
        }
    }

    // 메세지 전송
    fun sendMessage(content: String) {
        viewModelScope.launch(Dispatchers.IO) { // db는 io 작업
            val dto = ChatMessageDto(
                roomId = roomId,
                sender = me,
                content = content,
                createdAt = System.currentTimeMillis()
            )

            // api send and local insert
            val responseDto = chatMessageRepository.sendMessage(dto)

            // mqtt send
            if (responseDto != null) {
                val json = Gson().toJson(responseDto)
                MqttClient.publish("liontalk/rooms/$roomId/message", json)
            }

            // chatMessageDao.insert(messageEntity)

        }
    }

    // MQTT - methods
    private val topics = listOf("message", "typing", "enter", "leave")
    // MQTT 구독 및 메세지 수신 처리
    private fun subscribeToMqttTopics() {
        MqttClient.connect()
        MqttClient.setOnMessageReceived { topic, message ->
            handleInComingMqttMessage(topic, message)
        }
        topics.forEach {
            MqttClient.subscribe("liontalk/rooms/$roomId/$it")
        }
    }

    // MQTT 구독 해지
    private fun unsubscribeFromMqttTopics() {
        topics.forEach {
            MqttClient.unsubscribe("liontalk/rooms/$roomId/$it")
        }
    }

    // MQTT 수신 메세지 처리
    private fun handleInComingMqttMessage(topic: String, message: String) {
        when {
            topic.endsWith("/message") -> onReceivedMessage(message)
            topic.endsWith("/typing") -> onReceivedTyping(message)
            topic.endsWith("/enter") -> onReceivedTyping(message)
            topic.endsWith("/leave") -> onReceivedTyping(message)
        }
    }

    private fun onReceivedEnter(message: String){
        val dto =
    }

    private fun onReceivedMessage(message: String) {
        // 로컬에서 등록하기 전에 서버에서 id 받아서 해당 id로 로컬에서 저장할 것
        val dto = Gson().fromJson(message, ChatMessageDto::class.java)
        Log.d("ChatMessageEntity", "✅ avatarUrl from MQTT: ${dto.sender.avatarUrl}") // 🔍 추가

        viewModelScope.launch {
//            chatMessageDao.insert(dto.toEntity()) // 로컬에 저장
            chatMessageRepository.receiveMessage(dto)
        }
    }

    private fun onReceivedTyping(message: String) {
        val dto = Gson().fromJson(message, TypingMessageDto::class.java)
        if (dto.sender != me.name) {
            viewModelScope.launch {
                val event = if (dto.typing) ChatRoomEvent.TypingStarted(dto.sender)
                else ChatRoomEvent.TypingStopped

                _event.emit(event)
            }
        }
    }

    private var typing = false
    private var typingStopJob: Job? = null


    fun onTypingChanged(text: String) {
        if (text.isNotBlank() && !typing) {
            // publish typing status
            publishTypingStatus(true)
        }
        typingStopJob?.cancel()
        typingStopJob = viewModelScope.launch {
            delay(2000)
            typing = false
            publishTypingStatus(false)
        }
    }

    fun stopTyping() {
        typing = false
        publishTypingStatus(false)
        typingStopJob?.cancel()
    }

    private fun publishTypingStatus(isTyping: Boolean) {
        val json = Gson().toJson(TypingMessageDto(sender = me.name, isTyping))
        MqttClient.publish("liontalk/rooms/$roomId/typing", json)
    }
}