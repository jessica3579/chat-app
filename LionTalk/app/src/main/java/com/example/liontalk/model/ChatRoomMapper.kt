package com.example.liontalk.model

import com.example.liontalk.data.local.entity.ChatRoomEntity
import com.example.liontalk.data.remote.dto.ChatRoomDto

object ChatRoomMapper {
    fun ChatRoomDto.toEntity() = ChatRoomEntity(
        id, title, owner, users = emptyList(), createdAt
    )

    fun ChatRoomDto.toModel() = ChatRoom(
        id, title, owner, users = emptyList(), createdAt
    )

    fun ChatRoomEntity.toDto() = ChatRoomDto(
        id, title, owner, users = emptyList(), createdAt
    )
}