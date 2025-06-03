package com.example.proyecto.core


import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.proyecto.MainActivity
import com.example.proyecto.CHAT_NOTIFICATION_CHANNEL_ID
import com.example.proyecto.R
import com.example.proyecto.utils.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject


class ChatNotificationManagerFirestore(
    private val context: Context,
    private val currentUserId: String
) {
    private val db = FirebaseFirestore.getInstance()
    private var listenerRegistration: ListenerRegistration? = null


    fun startListeningForNewMessages() {
        // 1) Query para todos los docs en "messages" donde receiverId == currentUserId,
        //    ordenados por timestamp descendente.
        val messagesRef = db
            .collection("messages")
            .whereEqualTo("receiverId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        // 2) Agregamos un snapshot listener. DocumentChange.Type.ADDED → mensaje nuevo
        listenerRegistration = messagesRef.addSnapshotListener { snapshots, error ->
            if (error != null || snapshots == null) {
                // Si hay error o snapshots es null, salimos sin hacer nada
                return@addSnapshotListener
            }

            for (dc in snapshots.documentChanges) {
                if (dc.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                    // Se añadió un mensaje nuevo al query
                    val newMessage = dc.document.toObject<Message>()
                    // No notificamos si el mensaje lo envié yo mismo
                    if (newMessage.senderId != currentUserId) {
                        // 3) Obtenemos el nombre del remitente de Firestore
                        fetchUsername(newMessage.senderId) { senderName ->
                            // 4) Mostramos la notificación
                            showNewMessageNotification(
                                senderName = senderName,
                                messageText = newMessage.text
                            )
                        }
                    }
                }
            }
        }
    }


    fun stopListening() {
        listenerRegistration?.remove()
    }

    private fun fetchUsername(uid: String, callback: (String) -> Unit) {
        db.collection("usuarios")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val nombre = doc.getString("nombre") ?: uid
                callback(nombre)
            }
            .addOnFailureListener {
                callback(uid)
            }
    }


    private fun showNewMessageNotification(senderName: String, messageText: String) {
        // Construir el PendingIntent como antes…
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_action", "open_chat")
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationId = (System.currentTimeMillis() % 100000).toInt()
        val builder = NotificationCompat.Builder(context, CHAT_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(senderName)
            .setContentText(messageText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        val sdk = Build.VERSION.SDK_INT
        if (sdk < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }

}
