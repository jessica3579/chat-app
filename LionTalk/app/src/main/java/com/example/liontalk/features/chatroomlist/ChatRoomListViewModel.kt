package com.example.liontalk.features.chatroomlist

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liontalk.data.local.AppDatabase
import com.example.liontalk.data.local.entity.ChatRoomEntity
import com.example.liontalk.data.remote.dto.ChatMessageDto
import com.example.liontalk.data.remote.mqtt.MqttClient
import com.example.liontalk.data.repository.ChatMessageRepository
import com.example.liontalk.data.repository.ChatRoomRepository
import com.example.liontalk.data.repository.UserPreferenceRepository
import com.example.liontalk.model.ChatMessage
import com.example.liontalk.model.ChatRoomMapper.toDto
import com.example.liontalk.model.ChatUser
import com.google.gson.Gson
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient.Mqtt3SubscribeAndCallbackBuilder.Call.Ex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomListViewModel(application: Application) : ViewModel() {
    //    private val _state = MutableLiveData(ChatRoomListState())
//    val state: LiveData<ChatRoomListState> = _state

    private val _state = MutableStateFlow(ChatRoomListState())
    val state: StateFlow<ChatRoomListState> = _state

    private val chatRoomRepository = ChatRoomRepository(application.applicationContext)
    private val chatMessageRepository = ChatMessageRepository(application.applicationContext)

    private val userPreferenceRepository = UserPreferenceRepository.getInstance()
    val me: ChatUser get() = userPreferenceRepository.requireMe()


    init {
        loadChatRooms()
    }

    private fun loadChatRooms() {
        viewModelScope.launch {

            _state.value = _state.value.copy(isLoading = true)

            try {
                withContext(Dispatchers.IO) {
                    subscribeToMqttTopics()
                }
                withContext(Dispatchers.IO) {
                    chatMessageRepository.syncAllMessagesFromServer()
                }

                withContext(Dispatchers.IO) {
                    chatRoomRepository.syncFromServer()
                }

                chatRoomRepository.getChatRoomsFlow().collect { rooms ->
                    val joined = rooms.filter { it.users.any { p -> p.name == me.name } }
                    val notJoined = rooms.filter { it.users.none { p -> p.name == me.name } }
                    Log.d("ROOM", "me: $me")
                    Log.d("ROOM", "joined: $joined")
                    Log.d("ROOM", "not joined: $joined")

                    _state.value = _state.value.copy(
                        isLoading = false,
                        chatRooms = rooms,
                        joinedRooms = joined,
                        notJoinedRooms = notJoined
                    )
                }

            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }

        }
    }


    fun createChatRoom(title: String) {
        Log.d("ChatRoomListViewModel", title)
        viewModelScope.launch {
            try {
                Log.d("ChatRoomListViewModel", me.toString())

                val room = ChatRoomEntity(
                    title = title,
                    owner = me,
                    users = emptyList(),
                    createdAt = System.currentTimeMillis()
                )

                Log.d("ChatRoomListViewModel", room.toString())

                chatRoomRepository.createChatRoom(room.toDto())
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
            }

        }
    }

    fun switchTab(tab: ChatRoomTab) {
        _state.value = _state.value.copy(currentTab = tab)
    }


    // -------------------------MQTT----------------------------
    private val topics = listOf("message")
    private fun subscribeToMqttTopics() {
        MqttClient.connect()
        MqttClient.setOnMessageReceived { topic, message ->
            handleReceivedMessage(topic, message)
        }
        topics.forEach { MqttClient.subscribe("liontalk/rooms/+/$it") } // 룸 아이디를 무시하고 message라는 토픽을 다 구독함
    }

    private fun handleReceivedMessage(topic: String, message: String) {
        when {
            topic.endsWith("/message") -> onReceivedMessage(message)
        }
    }

    private fun onReceivedMessage(message: String) {
        try {
            val dto = Gson().fromJson(message, ChatMessageDto::class.java)
            viewModelScope.launch {
                val room = withContext(Dispatchers.IO) {
                    chatRoomRepository.getChatRoom(dto.roomId)
                }

                val unReadCount = withContext(Dispatchers.IO) {
                    chatMessageRepository.fetchUnReadCountFromServer(dto.roomId,
                        room?.lastReadMessageId
                    )
                }
                withContext(Dispatchers.IO) {
                    chatRoomRepository.updateUnReadCount(dto.roomId, unReadCount)
                }
            }
        } catch (e: Exception) {

        }
    }
    // -------------------------MQTT----------------------------

}