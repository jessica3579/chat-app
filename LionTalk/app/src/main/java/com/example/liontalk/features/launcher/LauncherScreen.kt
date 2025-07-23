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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.liontalk.data.repository.UserPreferenceRepository
import com.example.liontalk.ui.theme.navigation.Screen
import kotlinx.coroutines.delay

@Composable
fun LauncherScreen(navHostController: NavHostController) {
    val context = LocalContext.current

    var alpha by remember { mutableStateOf(0f) }

    val animationAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 800),
        label = "fade-in"
    )

    LaunchedEffect(Unit) {
        UserPreferenceRepository.init(context)
        val userPreferenceRepository = UserPreferenceRepository.getInstance()

        Log.d("Launcher", "⏳ Launcher 시작됨")

        delay(200)

        alpha = 1f

        Log.d("Launcher", "🌟 애니메이션 시작")


        delay(1500)
        Log.d("LauncherScreen","initialized before :${userPreferenceRepository.isInitialized}")


        if(!userPreferenceRepository.isInitialized){
            Log.d("Launcher", "📦 유저 데이터 로딩 중")
            userPreferenceRepository.loadUserFromStorage()
            Log.d("Launcher", "✅ 유저 데이터 로딩 완료")
        }

        val user = userPreferenceRepository.meOrNull
        Log.d("Launcher", "👤 사용자 정보: $user")


        val destination = if(user == null || user.name.isBlank()){
            Log.d("Launcher", "➡️ SettingScreen으로 이동")
            Screen.SettingScreen.route
        }else {
            Log.d("Launcher", "➡️ ChatRoomScreen으로 이동")

            Screen.ChatRoomListScreen.route
        }

        navHostController.navigate(destination){
            popUpTo("launcher") {inclusive = true}
        }
    }
    Box(
        modifier = Modifier.fillMaxSize().background(Color.White),
        contentAlignment = Alignment.Center
    ){
        Text(
            text = "LionTalk",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.alpha(animationAlpha)
        )
    }
}