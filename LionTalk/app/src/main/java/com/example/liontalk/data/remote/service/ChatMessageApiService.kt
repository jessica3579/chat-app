package com.example.liontalk.data.remote.service

import retrofit2.http.Query
import com.example.liontalk.data.remote.dto.ChatMessageDto
import com.example.liontalk.data.remote.dto.ChatRoomDto
import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ChatMessageApiService {
    @POST("messages")
    suspend fun sendMessage(@Body messageDto: ChatMessageDto): Response<ChatMessageDto>

    @GET("messages")
    suspend fun fetchMessages():List<ChatMessageDto>

    @GET("messages")
    suspend fun fetchMessagesByRoomId(@Query("roomId") roomId:Int): List<ChatMessageDto>
}