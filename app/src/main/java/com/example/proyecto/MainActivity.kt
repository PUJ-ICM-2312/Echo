package com.example.proyecto

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.proyecto.core.ChatNotificationManagerFirestore
import com.example.proyecto.core.FriendRequestsNotificationManager
import com.example.proyecto.navigation.NavigationStack
import com.google.firebase.auth.FirebaseAuth


const val CHAT_NOTIFICATION_CHANNEL_ID = "chat_channel"
const val REQUESTS_NOTIFICATION_CHANNEL_ID = "requests_channel"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannels()
        setContent {
            NotificationPermissionRequest()
            StartNotificationListeners()
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationStack()
                }
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal para mensajes
            val chatChannel = NotificationChannel(
                CHAT_NOTIFICATION_CHANNEL_ID,
                "Mensajes Nuevos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando recibes un mensaje nuevo"
            }

            val requestsChannel = NotificationChannel(
                REQUESTS_NOTIFICATION_CHANNEL_ID,
                "Solicitudes de Amistad",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones cuando recibes una solicitud de amistad"
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chatChannel)
            manager.createNotificationChannel(requestsChannel)
        }
    }
}


@Composable
private fun NotificationPermissionRequest() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val context = LocalContext.current
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {

            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) {
                    Toast.makeText(
                        context,
                        "Sin permiso de notificaciones, no verás alertas.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            LaunchedEffect(Unit) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}


@Composable
private fun StartNotificationListeners() {
    val context = LocalContext.current
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(currentUid) {
        if (!currentUid.isNullOrEmpty()) {

            ChatNotificationManagerFirestore(
                context.applicationContext,
                currentUid
            ).startListeningForNewMessages()

            FriendRequestsNotificationManager(
                context.applicationContext,
                currentUid
            ).startListeningForRequests()
        }
    }
}
