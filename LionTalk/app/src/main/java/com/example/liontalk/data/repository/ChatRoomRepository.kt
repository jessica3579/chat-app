package com.example.liontalk.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.example.liontalk.data.local.datasource.ChatMessageLocalDataSource
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
    private val chatMessageLocal = ChatMessageLocalDataSource(context)

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
    suspend fun syncFromServer() { // 서버에서 가져와서 dto를 entity로
        try {
            Log.d("ChattingRoom-Sync", "서버에서 채팅방 목록을 가져오는 중...")
            val remoteRooms = remote.fetchRooms()
            Log.d("ChattingRoom-Sync", "서버에서 ${remoteRooms.size}개의 채팅방을 가져옴")

            for (remoteRoom in remoteRooms) {
                val localRoom = local.getChatRoom(remoteRoom.id)

                val lastReadMessageId = localRoom?.lastReadMessageId ?:0

                val unReadCount = chatMessageLocal.getUnreadMessageCount(remoteRoom.id,lastReadMessageId)

                if (localRoom != null) {
                    // 기존 채팅방이 있으면 users만 업데이트
                    local.updateUsers(remoteRoom.id, remoteRoom.users)
                    Log.d("ChattingRoom-Sync", "기존 채팅방 '${remoteRoom.id}'의 참여자:${remoteRoom.users} 업데이트")
                    // 기존 채티방에 읽지 않은 메세지 카운트를 업데이트 한다.
                    local.updateUnReadCount(remoteRoom.id, unReadCount)
                    Log.d("ChattingRoom-Sync", "기존 채팅방 '${remoteRoom.id}'의 unReadCount:${unReadCount} 업데이트")
                } else {
                    // 없으면 새로 insert
                    val entity = ChatRoomEntity(
                        id = remoteRoom.id,
                        title = remoteRoom.title,
                        owner = remoteRoom.owner,
                        users = remoteRoom.users,
                        lastReadMessageId = lastReadMessageId,
                        unReadCount = unReadCount,
                        isLocked = remoteRoom.isLocked,
                        createdAt = remoteRoom.createdAt
                    )
                    local.insert(entity)
                    Log.d("ChattingRoom-Sync", "신규 채팅방 '${remoteRoom.id}' 추가")
                }
            }
            val dbCount = local.getCount()
            Log.d("ChattingRoom-Sync", "로컬 DB에 ${dbCount}개 저장 완료")

        } catch (e: Exception) {
            Log.e("ChattingRoom-Sync", "채팅방 동기화 중 오류 발생: ${e.message}", e)
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

    suspend fun updateLastReadMessageId(roomId: Int, lastReadMessageId: Int){
        local.updateLastReadMessageId(roomId, lastReadMessageId)
    }

    suspend fun updateUnReadCount(roomId: Int, unReadCount: Int){
        local.updateUnReadCount(roomId, unReadCount)
    }

    suspend fun updateLocalStatus(roomId: Int, isLocked: Boolean){
        try {
            val remoteRoom = remote.fetchRoom(roomId)
            val updated = remoteRoom.copy(isLocked = isLocked)
            val result = remote.updateRoom(updated) ?: throw Exception("방 잠금($isLocked) 실패")

            result?.let {
                local.updateLockStatus(roomId, isLocked)
            }
        }catch (e: Exception){
            Log.e("ROOM", "채팅방 상태 변경중 오류 발생 : ${e.message}")
        }
    }

    suspend fun getChatRoom(roomId: Int) : ChatRoom?{
        return local.getChatRoom(roomId)?.toModel()
    }


}