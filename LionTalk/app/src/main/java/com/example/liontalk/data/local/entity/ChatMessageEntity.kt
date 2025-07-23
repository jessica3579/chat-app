package com.example.liontalk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.liontalk.model.ChatUser

@Entity(tableName = "chat_message")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = false) val id: Int = 0,
    val roomId: Int,    //  방 id
    val sender: ChatUser, // 보낸 사람
    val content: String, // 보낸 메시지
    val createdAt: Long, // 시간
)