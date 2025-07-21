package com.example.liontalk.features.chatroom

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liontalk.data.local.AppDatabase
import com.example.liontalk.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ChatRoomViewModel(application: Application, private val roomId: Int): ViewModel() {
    private val chatMessageDao = AppDatabase.create(application).chatMessageDao()

   val messages : LiveData<List<ChatMessageEntity>> = chatMessageDao.getMessageForRoom(roomId)

    // 메세지 전송
    fun sendMessage(sender: String, content: String){
        viewModelScope.launch(Dispatchers.IO) { // db는 io 작업
            val messageEntity = ChatMessageEntity(
                roomId = roomId,
                sender = sender,
                content = content,
                createdAt = System.currentTimeMillis()
            )
            chatMessageDao.insert(messageEntity)

        }
    }
}