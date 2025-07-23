package com.example.liontalk.data.remote.dto

import com.example.liontalk.model.ChatUser

data class ChatMessageDto (
    val id: Int = 0,
    val roomId: Int,
    val sender: ChatUser,
    val content: String,
    val createdAt: Long
)