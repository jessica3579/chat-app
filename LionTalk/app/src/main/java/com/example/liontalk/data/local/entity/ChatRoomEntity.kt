package com.example.liontalk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.liontalk.model.ChatUser

@Entity(tableName = "chat_room")
data class ChatRoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title : String,
    val owner: ChatUser,
    val users: List<ChatUser> = emptyList(),
    val unReadCount: Int = 0,
    val lastReadMessageId: Int = 0,
    val isLocked: Boolean = false,
    val createdAt: Long
)
