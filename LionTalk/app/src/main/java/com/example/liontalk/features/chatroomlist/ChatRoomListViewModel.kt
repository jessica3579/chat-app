package com.example.liontalk.features.chatroomlist

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.liontalk.data.local.AppDatabase
import com.example.liontalk.data.local.entity.ChatRoomEntity
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient.Mqtt3SubscribeAndCallbackBuilder.Call.Ex
import kotlinx.coroutines.launch

class ChatRoomListViewModel(application: Application): ViewModel() {
    private val _state = MutableLiveData(ChatRoomListState())
    val state : LiveData<ChatRoomListState> = _state

    private val chatRoomDao = AppDatabase.create(application).chatRoomDao()

    init {
        loadChatRooms()
    }

    private fun loadChatRooms(){
        viewModelScope.launch {
            _state.value = _state.value?.copy(isLoading = true)
            try{
                chatRoomDao.getChatRooms().observeForever{ rooms ->
                    _state.postValue(
                        ChatRoomListState(
                            isLoading = false,
                            chatRooms = rooms
                        )
                    )
                }
            }catch (e: Exception){
                _state.value = _state.value?.copy(isLoading = false, error = e.message)
            }

        }
    }

    fun createChatRoom(title: String){
        viewModelScope.launch {
            try{
                val room = ChatRoomEntity(
                    title = title,
                    owner = "me",
                    users = emptyList(),
                    createdAt = System.currentTimeMillis()
                )
                chatRoomDao.insert(room)
            }catch (e:Exception){
                _state.value = _state.value?.copy(isLoading = false, error = e.message)
            }

        }
    }
}