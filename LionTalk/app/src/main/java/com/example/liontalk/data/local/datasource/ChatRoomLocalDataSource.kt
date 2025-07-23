package com.example.liontalk.data.local.datasource

import android.content.Context
import androidx.lifecycle.LiveData
import com.example.liontalk.data.local.AppDatabase
import com.example.liontalk.data.local.entity.ChatRoomEntity
import com.example.liontalk.model.ChatUser

class ChatRoomLocalDataSource(context: Context) {
    private val dao = AppDatabase.create(context).chatRoomDao()

    fun getChatRooms(): LiveData<List<ChatRoomEntity>> {
        return dao.getChatRooms()
    }

    fun getChatRoom(roomId: Int) : ChatRoomEntity{
        return dao.getChatRoom(roomId)
    }

    suspend fun insert(chatRoom : ChatRoomEntity){
        dao.insert(chatRoom)
    }

    suspend fun insertAll(chatRooms: List<ChatRoomEntity>) {
        dao.insertAll(chatRooms)
    }

    suspend fun delete(chatRoom: ChatRoomEntity){
        dao.delete(chatRoom)
    }

    suspend fun updateUsers(id: Int, users: List<ChatUser>){
        dao.updateUsers(id, users)
    }

    suspend fun clear(){
        dao.clear()
    }

    suspend fun getCount(): Int{
        return dao.getCount()
    }

}