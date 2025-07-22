package com.example.liontalk.features.chatroomlist

import androidx.lifecycle.LiveData
import com.example.liontalk.data.local.entity.ChatRoomEntity
import java.lang.Error

// 상태를 하나의 객체로 묶어두는 것이 유지보수에 좋음

data class ChatRoomListState(
    val isLoading : Boolean = false,
    val chatRooms : List<ChatRoomEntity> = emptyList(),
    val error: String? = null
)