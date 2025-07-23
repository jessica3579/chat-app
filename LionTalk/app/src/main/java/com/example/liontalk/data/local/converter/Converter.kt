package com.example.liontalk.data.local.converter

import android.util.Log
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.liontalk.model.ChatUser
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converter {

    private val gson = Gson()

    // List<String> -> jsonString to Room
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return Gson().toJson(value)
    }

    // RoomDB jsonString -> List<String>
    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    //    @TypeConverter
//    fun fromUser(value: ChatUser): String = gson.toJson(value)
//
//    @TypeConverter
//    fun toUser(value: String): ChatUser = gson.fromJson(value, ChatUser::class.java)
    @TypeConverter
    fun fromUser(value: ChatUser): String {
        Log.d("Converter", "🔥 fromUser: ${value.name}, ${value.avatarUrl}")
        return gson.toJson(value)
    }


    @TypeConverter
    fun toUser(value: String): ChatUser {
        val user = gson.fromJson(value, ChatUser::class.java)
        Log.d("Converter", "⭐ toUser: ${user.name}, ${user.avatarUrl}")
        return user
    }


    @TypeConverter
    fun toUserList(value: String): List<ChatUser> {
        val listType = object : TypeToken<List<ChatUser>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromUserList(value: List<ChatUser>): String {
        return gson.toJson(value)
    }
}
