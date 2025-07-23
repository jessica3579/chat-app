package com.example.liontalk.features.chatroom

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liontalk.data.local.entity.ChatMessageEntity
import com.example.liontalk.data.remote.dto.ChatMessageDto
import com.example.liontalk.data.remote.dto.PresenceMessageDto
import com.example.liontalk.data.remote.dto.TypingMessageDto
import com.example.liontalk.data.remote.mqtt.MqttClient
import com.example.liontalk.data.repository.ChatMessageRepository
import com.example.liontalk.data.repository.ChatRoomRepository
import com.example.liontalk.data.repository.UserPreferenceRepository
import com.example.liontalk.model.ChatMessage
import com.example.liontalk.model.ChatUser
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomViewModel(application: Application, private val roomId: Int) : ViewModel() {
    private val chatMessageRepository = ChatMessageRepository(application)
    private val chatRoomRepository = ChatRoomRepository(application.applicationContext)

    //    val messages: LiveData<List<ChatMessageEntity>> = chatMessageRepository.getMessageForRoom(roomId)

    // 시스템 메세지 리스트
    val _systemMessages = MutableStateFlow<List<ChatMessage>>(emptyList())

    // 채팅 메세지
    val messages: StateFlow<List<ChatMessage>> = combine(
        chatMessageRepository.getMessageForRoomFlow(roomId),
        _systemMessages
    ) { dbMessages, systemMessages ->
        (dbMessages + systemMessages).sortedBy { it.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
//        chatMessageRepository.getMessageForRoomFlow(roomId)
//            .stateIn(
//                scope = viewModelScope,
//                started = SharingStarted.WhileSubscribed(5000),
//                initialValue = emptyList()
//            )

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

            // 최초 채팅방 진입시 입장 이벤트 전송
            publishEnterStatus()
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

                // 메세지 입력 종료 퍼블리시
                publishTypingStatus(false)

                // 메세지 입력창 초기화 이벤트
                _event.emit(ChatRoomEvent.ClearInput)
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
            topic.endsWith("/enter") -> onReceivedEnter(message)
            topic.endsWith("/leave") -> onReceivedLeave(message)
        }
    }

    // 채팅방 입장 메세지 핸들러
    private fun onReceivedEnter(message: String) {
        val dto = Gson().fromJson(message, PresenceMessageDto::class.java)
        if (dto.sender != me.name) {
            viewModelScope.launch {
                _event.emit(ChatRoomEvent.ChatRoomEnter(dto.sender))

                postSystemMessage("${dto.sender} 님이 입장하셨습니다.")

                _event.emit(ChatRoomEvent.ScrollToBottom)
            }
        }
    }

    // 채팅방 퇴장 메세지 핸들러
    private fun onReceivedLeave(message: String) {
        val dto = Gson().fromJson(message, PresenceMessageDto::class.java)
        if (dto.sender != me.name) {
            viewModelScope.launch {
                _event.emit(ChatRoomEvent.ChatRoomLeave(dto.sender))

                postSystemMessage("${dto.sender} 님이 퇴장하셨습니다.")

                _event.emit(ChatRoomEvent.ScrollToBottom)
            }
        }
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

    private fun postSystemMessage(content: String){
        val systemMessage = ChatMessage(
            id = -1,
            roomId = roomId,
            sender = me,
            content = content,
            type = "system",
            createdAt = System.currentTimeMillis()
        )
        _systemMessages.value = _systemMessages.value + systemMessage
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

    // 채팅방 나가기
    fun leaveRoom(onComplete: () -> Unit) {
        viewModelScope.launch {
            publishLeaveStatus()

            onComplete()
        }
    }

    // 메세지 입력 이벤트 퍼블리시
    private fun publishTypingStatus(isTyping: Boolean) {
        val json = Gson().toJson(TypingMessageDto(sender = me.name, isTyping))
        MqttClient.publish("liontalk/rooms/$roomId/typing", json)
    }

    // 채팅방 입장 이벤트 퍼블리시
    private fun publishEnterStatus() {
        val json = Gson().toJson(PresenceMessageDto(me.name))
        MqttClient.publish("liontalk/rooms/$roomId/enter", json)

        // 서버및 로컬 입장 처리
        viewModelScope.launch {
            chatRoomRepository.enterRoom(me, roomId)
        }
    }

    // 채팅방 퇴장 이벤트 퍼블리시
    private fun publishLeaveStatus() {
        val json = Gson().toJson(PresenceMessageDto(me.name))
        MqttClient.publish("liontalk/rooms/$roomId/leave", json)
    }
}