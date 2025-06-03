package com.example.proyecto.core

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.proyecto.MainActivity
import com.example.proyecto.REQUESTS_NOTIFICATION_CHANNEL_ID
import com.example.proyecto.data.AmigosRepository
import com.example.proyecto.core.NotificationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.proyecto.R
import com.example.proyecto.utils.model.Message
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

class ChatNotificationManager(
    private val context: Context,
    private val currentUserId: String
) {
    private val database = FirebaseDatabase.getInstance().reference






    private fun fetchUsername(uid: String, callback: (String) -> Unit) {
        database.child("usuarios").child(uid).child("nombre")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombre = snapshot.getValue(String::class.java) ?: uid
                    callback(nombre)
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(uid)
                }
            })
    }
}


class FriendRequestsNotificationManager(
    private val context: Context,
    private val currentUserId: String
) {
    private val database = FirebaseDatabase.getInstance().reference

    fun startListeningForRequests() {
        val requestsRef = database
            .child("usuarios")
            .child(currentUserId)
            .child("receivedRequests")

        requestsRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val solicitanteUid = snapshot.key ?: return

                fetchUsername(solicitanteUid) { nombreSolicitante ->
                    showNewRequestNotification(nombreSolicitante)
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun fetchUsername(uid: String, callback: (String) -> Unit) {
        database.child("usuarios").child(uid).child("nombre")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombre = snapshot.getValue(String::class.java) ?: uid
                    callback(nombre)
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(uid)
                }
            })
    }

    private fun showNewRequestNotification(solicitanteName: String) {
        val title = "Nueva solicitud de amistad"
        val text = "$solicitanteName te ha enviado una solicitud."

        // Intent para abrir MainActivity
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("notification_action", "open_requests")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationId = (System.currentTimeMillis() % 100000).toInt()

        val builder = NotificationCompat.Builder(context, REQUESTS_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)


        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

}
