package com.example.liontalk.features.launcher

import android.graphics.Paint.Align
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.liontalk.data.remote.mqtt.MqttClient
import com.example.liontalk.data.repository.UserPreferenceRepository
import com.example.liontalk.ui.theme.navigation.Screen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun LauncherScreen(navHostController: NavHostController) {
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()
    var alpha by remember { mutableStateOf(0f) }

    val animationAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 800),
        label = "fade-in"
    )

    LaunchedEffect(Unit) {
        UserPreferenceRepository.init(context)
        val userPreferenceRepository = UserPreferenceRepository.getInstance()

        delay(200)
        alpha = 1f

        delay(1500)
//        if (!userPreferenceRepository.isInitialized)
        userPreferenceRepository.loadUserFromStorage()

        val user = userPreferenceRepository.meOrNull

        val destination = if (user == null || user.name.isBlank()) {
            Screen.SettingScreen.route
        } else {
            Screen.ChatRoomListScreen.route
        }

        withContext(Dispatchers.IO) {
            MqttClient.connect()
            true
        }

        navHostController.navigate(destination) {
            popUpTo("launcher") { inclusive = true }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "LionTalk",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.alpha(animationAlpha)
        )
    }
}