package com.example.proyecto.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto.ui.components.obtenerUsernamePorUid
import com.example.proyecto.utils.model.Message
import com.example.proyecto.utils.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectChatScreen(
    friendUid: String,
    onNavigateBack: () -> Unit,
    chatViewModel: ChatViewModel = viewModel()
) {
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val messageText by chatViewModel.messageText.collectAsStateWithLifecycle()
    val error by chatViewModel.error.collectAsStateWithLifecycle()
    val userDetails by chatViewModel.userDetails.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var username by remember { mutableStateOf<String?>(null) }


    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                chatViewModel.setChatScreenActive(true)
            } else if (event == Lifecycle.Event.ON_STOP) {
                chatViewModel.setChatScreenActive(false)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            chatViewModel.setChatScreenActive(false)
        }
    }

    // Filtrar solo mensajes que involucren a este friendUid
    val conversation = remember(messages, friendUid, currentUserId) {
        messages.filter { msg ->
            (msg.senderId == currentUserId && msg.receiverId == friendUid) ||
                    (msg.senderId == friendUid && msg.receiverId == currentUserId)
        }
    }

    LaunchedEffect(conversation.size) {
        if (conversation.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(conversation.size - 1)
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            chatViewModel.clearError()
        }
    }

    LaunchedEffect(friendUid) {
        obtenerUsernamePorUid(friendUid) { result ->
            username = result
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Chat con ${username ?: (friendUid.take(6) + "…")}")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            MessageInput(
                text = messageText,
                onTextChange = chatViewModel::onMessageTextChange,
                onSendClick = {
                    chatViewModel.sendDirectMessage(toUid = friendUid)
                },
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime))
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(conversation, key = { it.timestamp?.time ?: it.text }) { message ->
                    MessageBubble(
                        message = message,
                        isSentByCurrentUser = message.senderId == currentUserId,
                        senderDetails = userDetails[message.senderId]
                    )
                }
            }
        }
    }
}
