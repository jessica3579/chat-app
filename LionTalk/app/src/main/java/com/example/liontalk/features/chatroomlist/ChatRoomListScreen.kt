package com.example.liontalk.features.chatroomlist

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.liontalk.ui.theme.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomListScreen(navController: NavHostController) {
    val context =
        LocalContext.current // // View -> ViewMode -> Repository -> Local Data Source -> AppDataBase의 room 생성에 context가 필요하기 때문
    val viewModel = remember {
        ChatRoomListViewModel(context.applicationContext as Application)
    }

//    val state by viewModel.state.observeAsState(ChatRoomListState()) // observeAsState로 ChatRoomListState()의 모든 상태 감지!! live data인 state가 변경되면 자동으로 호출됨
    val state by viewModel.state.collectAsState()

    var newRoomName by remember { mutableStateOf("") }
    var showAddRoomDialog by remember { mutableStateOf(false) }

    val tabTitles = mapOf(
        ChatRoomTab.JOINED to "참여중",
        ChatRoomTab.NOT_JOINED to "미참여"
    )

    Scaffold(
        topBar = {
//            TopAppBar(title = { Text("채팅방 목록") })
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "LionTalk",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        showAddRoomDialog = true
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "채팅방 생성")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("setting")
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                }
            )
        },
        content = { padding -> // Scaffold가 생성한 자동 여백
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                TabRow(selectedTabIndex = state.currentTab.ordinal) {
                    ChatRoomTab.values().forEach { tab ->
                        Tab(
                            selected = state.currentTab == tab,
                            onClick = { viewModel.switchTab(tab) },
                            text = { Text(text = tabTitles[tab] ?: tab.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (state.chatRooms.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("채팅방이 없습니다.")
                    }

                } else {
                    val rooms = when (state.currentTab) {
                        ChatRoomTab.JOINED -> state.joinedRooms
                        ChatRoomTab.NOT_JOINED -> state.notJoinedRooms
                    }

                    if (rooms.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("채팅방이 없습니다.")
                        }
                    } else {
                        LazyColumn {
                            items(rooms) { room ->
//                            Text(text = room.title,)
                                ChatRoomItem(room = room,
                                    isOwner = room.owner.name == viewModel.me.name,
                                    onClick = {
                                    navController.navigate(
                                        Screen.ChatRoomScreen.createRoute(room.id)
                                    )
                                },
                                    onLongPressDelete = {},
                                    onLongPressLock = {}
                                    )

                            }
                        }
                    }


                }

            }

        }
    )
    if (showAddRoomDialog) {
        AlertDialog(
            onDismissRequest = { showAddRoomDialog = false },
            title = { Text("채팅방 생성") },
            text = {
                OutlinedTextField(
                    value = newRoomName,
                    onValueChange = { newRoomName = it },
                    label = { Text("채팅방 제목") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newRoomName.isNotBlank()) {
                        viewModel.createChatRoom(newRoomName)
                        newRoomName = ""
                        showAddRoomDialog = false
                    }
                }) {
                    Text("생성")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddRoomDialog = false
                }) {
                    Text("취소")
                }
            }
        )
    }
}