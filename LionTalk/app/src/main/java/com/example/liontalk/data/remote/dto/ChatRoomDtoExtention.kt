package com.example.liontalk.data.remote.dto

import com.example.liontalk.model.ChatUser

// 입장시 기존 룸 정보에 해당 사용자 추가
fun ChatRoomDto.addUserIfNotExists(user: ChatUser): ChatRoomDto{
    val updatedUsers = this.users.toMutableList().apply {
        if(none{ it.name == user.name}) add(user)
    }
    return this.copy(users = updatedUsers)
}

// 퇴장시 기존 룸 정보에서 해당 사용자 정보를 제거
fun ChatRoomDto.removeUser(user: ChatUser): ChatRoomDto{
    val updatedUsers = this.users.filterNot { it.name == user.name }
    return this.copy(users = updatedUsers)
}