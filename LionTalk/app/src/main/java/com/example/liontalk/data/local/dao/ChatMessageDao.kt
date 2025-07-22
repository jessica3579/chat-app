package com.example.liontalk.data.local.dao

import android.adservices.adid.AdId
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.liontalk.data.local.entity.ChatMessageEntity
import retrofit2.http.DELETE

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("SELECT * FROM chat_message WHERE roomId = :roomId ORDER BY id DESC")
    fun getMessageForRoom(roomId: Int): LiveData<List<ChatMessageEntity>>

    @Query("DELETE FROM chat_message")
    suspend fun clear()
}