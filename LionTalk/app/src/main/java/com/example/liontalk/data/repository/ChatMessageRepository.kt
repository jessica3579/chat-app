package com.example.liontalk.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.liontalk.data.local.datasource.ChatMessageLocalDataSource
import com.example.liontalk.data.local.entity.ChatMessageEntity
import com.example.liontalk.data.remote.datasource.ChatMessageRemoteDataSource
import com.example.liontalk.data.remote.dto.ChatMessageDto
import com.example.liontalk.model.ChatMessageMapper.toEntity
import kotlinx.coroutines.flow.Flow

class ChatMessageRepository(context: Context) {
    private val remote = ChatMessageRemoteDataSource()
    private val local = ChatMessageLocalDataSource(context)

    suspend fun clearLocalDB(){
        local.clear()
    }

    // 현재 로컬 db에 저장된 메세지 목록 가져오기
//    fun getMessageForRoom(roomId: Int): LiveData<List<ChatMessageEntity>>{
//        return local.getMessageForRoomFlow(roomId)
//    }

    fun getMessageForRoomFlow(roomId: Int): Flow<List<ChatMessageEntity>> {
        return local.getMessageForRoomFlow(roomId)
    }


    // API 서버로 메세지를 보내고 로컬 db에 저장
    suspend fun sendMessage(message: ChatMessageDto) : ChatMessageDto? {
        try{
            val result = remote.sendMessage(message)
            result?.let{
                local.insert(it.toEntity())
                return it
            }
        }catch (e: Exception){
            Log.e("ChatMessageRepository", "${e.message}")
        }
        return null
    }

    // MQTT 수신 메세지 로컬 DB 저장
    suspend fun receiveMessage(message: ChatMessageDto){
        local.insert(message.toEntity())
    }
}