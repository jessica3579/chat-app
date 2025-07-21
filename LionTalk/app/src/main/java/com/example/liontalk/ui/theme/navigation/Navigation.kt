package com.example.liontalk.ui.theme.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.liontalk.features.chatroom.ChatRoomScreen
import com.example.liontalk.features.chatroomlist.ChatRoomListScreen

@Composable
fun ChatAppNavigation(navController: NavHostController){
    NavHost(
        navController = navController,
        startDestination = Screen.ChatRoomListScreen.route
    ){
        composable(Screen.ChatRoomListScreen.route){
            ChatRoomListScreen(navController)
        }

        composable(Screen.ChatRoomScreen.route) { backStackentry ->
            val roomId = backStackentry.arguments?.getString("roomId")?.toIntOrNull()
            if (roomId != null) {
                ChatRoomScreen(roomId)
            }

        }
    }
}