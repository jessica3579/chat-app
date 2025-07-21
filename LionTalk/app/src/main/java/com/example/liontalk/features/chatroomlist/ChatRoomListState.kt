package com.example.liontalk.features.chatroomlist

import androidx.lifecycle.LiveData
import com.example.liontalk.data.local.entity.ChatRoomEntity
import java.lang.Error

data class ChatRoomListState(
    val isLoading : Boolean = false,
    val chatRooms : List<ChatRoomEntity> = emptyList(),
    val error: String? = null
)