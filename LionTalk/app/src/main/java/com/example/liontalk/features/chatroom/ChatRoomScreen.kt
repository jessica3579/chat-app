package com.example.liontalk.features.chatroom

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.liontalk.features.chatroom.components.ChatMessageItem
import com.example.liontalk.features.chatroomlist.ChatRoomItem
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(roomId: Int) {
    val context = LocalContext.current
    val viewModel = remember { ChatRoomViewModel(context.applicationContext as Application, roomId) }

//    val messages by viewModel.messages.observeAsState(emptyList())
    val messages by viewModel.messages.collectAsState()
    val inputMessage = remember { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    val  typingUser = remember { mutableStateOf<String?>(null) }
    val eventFlow = viewModel.event
    LaunchedEffect(Unit) {
        eventFlow.collectLatest{ event ->
            when(event){
                is ChatRoomEvent.TypingStarted -> {
//                    Toast.makeText(context, "${event.sender}가 메세지를 입력 합니다.", Toast.LENGTH_SHORT).show()
                    typingUser.value = event.sender
                }
                is ChatRoomEvent.TypingStopped -> {
                    typingUser.value = null
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("채팅방 $roomId") })
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .navigationBarsPadding()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    items(messages) { message ->
                        // TODO
                        ChatMessageItem(message, viewModel.me.name == message.sender.name)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    if(typingUser.value != null){
                        Text(
                            text = "${typingUser.value}님이 입력중...",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    OutlinedTextField(
                        value = inputMessage.value,
                        onValueChange = {
                            inputMessage.value = it
                            viewModel.onTypingChanged(it)
                            if (it.isBlank()) {
                                viewModel.stopTyping()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                        placeholder = { Text("메세지 입력") },
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (inputMessage.value.isNotBlank()) {
                                viewModel.sendMessage(inputMessage.value)
                            }
                        },
                        modifier = Modifier
                            .height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "전송")
                    }
                }
            }
        }
    )

}