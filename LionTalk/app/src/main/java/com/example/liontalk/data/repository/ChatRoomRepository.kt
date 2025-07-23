package com.example.liontalk.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.liontalk.data.local.datasource.ChatRoomLocalDataSource
import com.example.liontalk.data.local.entity.ChatRoomEntity
import com.example.liontalk.data.remote.datasource.ChatRoomRemoteDataSource
import com.example.liontalk.data.remote.dto.ChatRoomDto
import com.example.liontalk.data.remote.dto.addUserIfNotExists
import com.example.liontalk.model.ChatRoom
import com.example.liontalk.model.ChatRoomMapper.toEntity
import com.example.liontalk.model.ChatRoomMapper.toModel
import com.example.liontalk.model.ChatUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRoomRepository(context: Context) {
    private val remote = ChatRoomRemoteDataSource()
    private val local = ChatRoomLocalDataSource(context)

    // local room db에서 chat room entity 목록을 가져온다.
    fun getChatRoomEntities(): LiveData<List<ChatRoomEntity>>{
        return local.getChatRooms()
    }

    fun getChatRoomsFlow(): Flow<List<ChatRoom>> {
        return local.getChatRoomsFlow().map { it.mapNotNull { entity -> entity.toModel() } }
    }

    suspend fun createChatRoom(chatRoom: ChatRoomDto){
        val chatroomDto = remote.createRoom(chatRoom)
        if (chatroomDto != null) {
            local.insert(chatroomDto.toEntity())
        }
    }

    suspend fun deleteChatRoomToRemote(roomId: Int){
        remote.deleteRoom(roomId)
    }

    // sync : remote to local
    suspend fun syncFromServer(){ // 서버에서 가져와서 dto를 entity로
        try {
            Log.d("Sync", "서버에서 채팅방 목록을 가져오는중...")
            val remoteRooms = remote.fetchRooms()
            Log.d("Sync", "${remoteRooms.size}개의 채팅방을 가져옴 ")
            val entities = remoteRooms.map { it.toEntity()} // dto를 entity로
            Log.d("Sync", "${entities.size}개의 Entity 변환")

            local.clear()

            Log.d("Sync", "로컬 DB에 채팅방 데이터 저장... 중")
            local.insertAll(entities)
            Log.d("Sync", "로컬 db 저장 완료")

            val dbCount = local.getCount()
            Log.d("Sync", "로컬 db 저장 완료 : $dbCount")

        }catch (e: Exception){
           throw e
        }
    }

    // 서버 및 로컬 room db 입장 처리
    suspend fun enterRoom(user: ChatUser, roomId: Int): ChatRoom {
        // 1. 서버로 부터 최신 룸 정보를 가져옴
        val remoteRoom = remote.fetchRoom(roomId)
        Log.d("Suji", "original room: $remoteRoom")

        val requestDto = remoteRoom.addUserIfNotExists(user)

        val updatedRoom = remote.updateRoom(requestDto)
        Log.d("Suji", "updated room: $remoteRoom")

        if(updatedRoom != null){
            local.updateUsers(roomId, updatedRoom.users)
        }
        return updatedRoom?.toModel() ?: throw Exception("서버 입장 처리 실패")

    }



}