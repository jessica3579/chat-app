package com.example.liontalk.features.chatroom

sealed class ChatRoomEvent {
    data class TypingStarted(val sender: String): ChatRoomEvent()
    object TypingStopped: ChatRoomEvent()

}