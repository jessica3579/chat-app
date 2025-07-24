package com.example.liontalk.data.local.datasource

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.liontalk.data.local.AppDatabase
import com.example.liontalk.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

class ChatMessageLocalDataSource(context: Context) {
    private val dao = AppDatabase.create(context).chatMessageDao()

    suspend fun clear(){
        dao.clear()
    }

    suspend fun insert(message: ChatMessageEntity){
        dao.insert(message)
    }

    fun getMessageForRoom(roomId: Int): LiveData<List<ChatMessageEntity>> {
        return dao.getMessageForRoom(roomId)
    }

    fun getMessageForRoomFlow(roomId: Int): Flow<List<ChatMessageEntity>> {
        return dao.getMessageForRoomFlow(roomId)
    }

    suspend fun getMessages(roomId: Int) : List<ChatMessageEntity> {
        return dao.getMessages(roomId)
    }

    suspend fun getLatestMessage(roomId: Int): ChatMessageEntity ? {
        return dao.getLatestMessage(roomId)
    }

    suspend fun deleteMessagesByRoomId(roomId:Int) {
        dao.deleteMessagesByRoomId(roomId)
    }

    suspend fun insertAll(messages: List<ChatMessageEntity>) {
        dao.insertAll(messages)
    }

    suspend fun getUnreadMessageCount(roomId: Int, lastReadMessageId: Int): Int {
        return dao.getUnreadMessageCount(roomId,lastReadMessageId)
    }
}