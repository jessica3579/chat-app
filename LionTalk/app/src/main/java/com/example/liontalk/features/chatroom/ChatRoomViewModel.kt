package com.example.liontalk.features.chatroom

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liontalk.data.local.AppDatabase
import com.example.liontalk.data.local.dao.ChatMessageDao
import com.example.liontalk.data.local.entity.ChatMessageEntity
import com.example.liontalk.data.remote.dto.ChatMessageDto
import com.example.liontalk.data.remote.mqtt.MqttClient
import com.example.liontalk.data.repository.ChatMessageRepository
import com.example.liontalk.model.ChatMessageMapper.toEntity
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatRoomViewModel(application: Application, private val roomId: Int): ViewModel() {
//    private val chatMessageDao = AppDatabase.create(application).chatMessageDao()
    private val chatMessageRepository = ChatMessageRepository(application)

//   val messages : LiveData<List<ChatMessageEntity>> = chatMessageDao.getMessageForRoom(roomId)
    val messages : LiveData<List<ChatMessageEntity>> = chatMessageRepository.getMessageForRoom(roomId)

    init {
        viewModelScope.launch {
            chatMessageRepository.clearLocalDB()

            withContext(Dispatchers.IO) {
                MqttClient.connect()
            }

            withContext(Dispatchers.IO){
                subscribeToMqttTopics()
            }
        }
    }

    // 메세지 전송
    fun sendMessage(sender: String, content: String){
        viewModelScope.launch(Dispatchers.IO) { // db는 io 작업
            val dto = ChatMessageDto(
                roomId = roomId,
                sender = sender,
                content = content,
                createdAt = System.currentTimeMillis()
            )

            // api send and local insert
            val responseDto = chatMessageRepository.sendMessage(dto)

            // mqtt send
            if(responseDto != null){
                val json = Gson().toJson(responseDto)
                MqttClient.publish("liontalk/rooms/$roomId/message", json)
            }

            // chatMessageDao.insert(messageEntity)

        }
    }

    // MQTT - methods
    private val topics = listOf("message", "enter")
    // MQTT 구독 및 메세지 수신 처리
    private fun subscribeToMqttTopics(){
        MqttClient.connect()
        MqttClient.setOnMessageReceived{ topic, message -> handleInComingMqttMessage(topic, message) }
        topics.forEach{
            MqttClient.subscribe("liontalk/rooms/$roomId/$it")
        }
    }

    // MQTT 구독 해지
    private fun unsubscribeFromMqttTopics(){
        topics.forEach{
            MqttClient.unsubscribe("liontalk/rooms/$roomId/$it")
        }
    }

    // MQTT 수신 메세지 처리
    private fun handleInComingMqttMessage(topic: String, message: String){
        when{
            topic.endsWith("/message") -> onReceivedMessage(message)
        }
    }

    private fun onReceivedMessage(message: String){
        // 로컬에서 등록하기 전에 서버에서 id 받아서 해당 id로 로컬에서 저장할 것
        val dto = Gson().fromJson(message, ChatMessageDto::class.java)

        viewModelScope.launch {
//            chatMessageDao.insert(dto.toEntity()) // 로컬에 저장
            chatMessageRepository.receiveMessage(dto)
        }
    }
}