package com.example.liontalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.liontalk.data.remote.mqtt.MqttClient
import com.example.liontalk.ui.theme.LionTalkTheme
import com.example.liontalk.ui.theme.navigation.ChatAppNavigation

class MainActivity : ComponentActivity() { // 컴포즈를 이용하는 액티비티는 CompoenentActivity 상속 받음
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { // 화면 출력 함수
            val navController = rememberNavController()
            ChatAppNavigation(navController = navController)
        }
    }

    override fun onStop() {
        super.onStop()
        MqttClient.disconnect()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    LionTalkTheme {
        Greeting("Android")
    }
}