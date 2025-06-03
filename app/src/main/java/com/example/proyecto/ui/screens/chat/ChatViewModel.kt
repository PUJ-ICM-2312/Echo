package com.example.proyecto.ui.screens.chat // Asegúrate que el paquete sea correcto

import android.util.Log
// import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.snapshots // Asegúrate que este import esté
import com.example.proyecto.utils.model.Message
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.async // Importa async para búsquedas paralelas

import android.app.Application // Necesario para el contexto
import androidx.lifecycle.AndroidViewModel // Cambiar ViewModel a AndroidViewModel
import com.example.proyecto.core.NotificationUtils // Importa tu utilidad
import com.example.proyecto.utils.model.Amigo
import com.example.proyecto.utils.model.Ubicacion
import com.example.proyecto.utils.model.Usuario
import com.google.firebase.database.FirebaseDatabase
import java.util.Date

// --- CAMBIO: Hereda de AndroidViewModel para acceder al Application Context ---
class ChatViewModel(application: Application) : AndroidViewModel(application) {
// --- FIN CAMBIO ---

    private val db: FirebaseFirestore = Firebase.firestore
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Flow para el texto actual en el campo de entrada
    private val _messageText = MutableStateFlow("")
    val messageText: StateFlow<String> = _messageText.asStateFlow()

    // Flow para la lista de mensajes (se actualiza en tiempo real)
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // Flow para errores
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // --- NUEVO: Estado para guardar detalles de usuarios ---
    private val _userDetails = MutableStateFlow<Map<String, Usuario>>(emptyMap())
    val userDetails: StateFlow<Map<String, Usuario>> = _userDetails.asStateFlow()
    // Guarda los IDs de los que ya intentamos buscar para no repetir
    private val fetchedUserIds = mutableSetOf<String>()
    // --- FIN NUEVO ---


    // --- NUEVO: Estado para saber si la pantalla de chat está activa ---
    private val _isChatScreenActive = MutableStateFlow(false)
    val isChatScreenActive: StateFlow<Boolean> = _isChatScreenActive.asStateFlow()

    fun setChatScreenActive(isActive: Boolean) {
        _isChatScreenActive.value = isActive
        if(isActive) {
            Log.d("ChatViewModel", "Chat screen is NOW ACTIVE")
        } else {
            Log.d("ChatViewModel", "Chat screen is NOW INACTIVE")
        }
    }
    // --- FIN NUEVO ---

    private var previousMessageCount = 0 // Para detectar nuevos mensajes

    init {
        listenForMessages()
    }


    // Actualiza el texto del mensaje
    fun onMessageTextChange(newText: String) {
        _messageText.value = newText
    }

    fun sendDirectMessage(toUid: String) {
        val textToSend = _messageText.value.trim()
        val currentUserId = auth.currentUser?.uid ?: return

        if (textToSend.isBlank()) return

        _messageText.value = ""
        _error.value = null


        val newMessage = Message(
            text = textToSend,
            senderId = currentUserId,
            receiverId = toUid,
            timestamp = Date()
        )
        viewModelScope.launch {
            try {
                db.collection("messages").add(newMessage).await()
            } catch (e: Exception) {
                _error.value = "Error enviando mensaje: ${e.message}"
            }
        }
    }
    private fun fetchUserDetailsIfNeeded(messages: List<Message>) {
        val currentUserId = auth.currentUser?.uid
        val senderIdsToFetch = messages
            .map { it.senderId }
            .distinct()
            .filter { it.isNotBlank() && it != currentUserId && !fetchedUserIds.contains(it) }
            .toSet()

        if (senderIdsToFetch.isNotEmpty()) {
            fetchedUserIds.addAll(senderIdsToFetch)

            senderIdsToFetch.forEach { userId ->
                val userRef = FirebaseDatabase.getInstance()
                    .getReference("usuarios")
                    .child(userId)

                userRef.get().addOnSuccessListener { snapshot ->
                    if (!snapshot.exists()) {
                        // Si no existe, permitimos reintentar más tarde
                        fetchedUserIds.remove(userId)
                        return@addOnSuccessListener
                    }

                    // 1) Campos primitivos de Usuario
                    val uid              = snapshot.child("uid").getValue(String::class.java) ?: userId
                    val nombre           = snapshot.child("nombre").getValue(String::class.java) ?: ""
                    val cedula           = snapshot.child("cedula").getValue(String::class.java) ?: ""
                    val username         = snapshot.child("username").getValue(String::class.java) ?: ""
                    val correoElectronico= snapshot.child("correoElectronico").getValue(String::class.java) ?: ""
                    val fotoDePerfilUrl  = snapshot.child("fotoDePerfilUrl").getValue(String::class.java) ?: ""
                    val telefono         = snapshot.child("telefono").getValue(String::class.java) ?: ""
                    val conectado        = snapshot.child("conectado").getValue(Boolean::class.java) ?: false

                    // 2) Lectura “dual” del objeto Ubicacion en campo "latitud"
                    val latNode = snapshot.child("latitud")
                    var ubicacionObj: Ubicacion? = null

                    if (latNode.hasChild("latitude") && latNode.hasChild("longitude")) {
                        // Caso A: el nodo "latitud" es un objeto { "latitude": x, "longitude": y }
                        ubicacionObj = latNode.getValue(Ubicacion::class.java)
                    } else {
                        // Caso B: "latitud" es un Double primitivo, y "longitud" está en otro campo
                        val primitiveLat = latNode.getValue(Double::class.java)
                        val primitiveLng = snapshot.child("longitud").getValue(Double::class.java)
                        if (primitiveLat != null && primitiveLng != null) {
                            ubicacionObj = Ubicacion(primitiveLat, primitiveLng)
                        }
                    }

                    // 3) Lectura de “amigos” (mapa de Amigo) si lo necesitas
                    val amigosSnapshot = snapshot.child("amigos")
                    val amigosMap: Map<String, Amigo> = if (amigosSnapshot.exists()) {
                        // Asumiendo que Amigo es también un data class serializable
                        amigosSnapshot.children.associate {
                            val key = it.key ?: ""
                            val amigoObj = it.getValue(Amigo::class.java)
                            key to (amigoObj ?: Amigo())
                        }
                    } else {
                        emptyMap()
                    }

                    // 4) Construcción manual de la instancia Usuario
                    val user = Usuario(
                        uid             = uid,
                        nombre          = nombre,
                        cedula          = cedula,
                        username        = username,
                        correoElectronico = correoElectronico,
                        fotoDePerfilUrl = fotoDePerfilUrl,
                        telefono        = telefono,
                        conectado       = conectado,
                        latitud         = ubicacionObj,
                        amigos          = amigosMap
                    )

                    // 5) Actualizamos nuestro Flow con el Usuario recién creado
                    _userDetails.value = _userDetails.value + (userId to user)
                }.addOnFailureListener {
                    // Si falla la lectura, permitimos reintentar la próxima vez
                    fetchedUserIds.remove(userId)
                }
            }
        }
    }


    private fun listenForMessages() {
        db.collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(100)
            .snapshots()
            .map { snapshot -> snapshot.toObjects<Message>() }
            .onEach { currentMessageList ->
                // --- LÓGICA PARA DETECTAR NUEVOS MENSAJES Y NOTIFICAR ---
                if (previousMessageCount > 0 && currentMessageList.size > previousMessageCount) {
                    // Hay nuevos mensajes desde la última actualización
                    val newMessages = currentMessageList.takeLast(currentMessageList.size - previousMessageCount)
                    newMessages.forEach { newMessage ->
                        // No notificar si el mensaje es del usuario actual o si el chat está activo
                        if (newMessage.senderId != auth.currentUser?.uid && !_isChatScreenActive.value) {
                            val senderDetails = _userDetails.value[newMessage.senderId]
                            val senderName = senderDetails?.nombre?.takeIf { it.isNotBlank() }
                                ?: senderDetails?.correoElectronico
                                ?: "Someone" // Fallback

                            Log.d("ChatViewModel", "New message from $senderName, chat inactive. Showing notification.")
                            NotificationUtils.showNewMessageNotification(
                                getApplication<Application>().applicationContext, // Contexto de la aplicación
                                senderName,
                                newMessage.text
                            )
                        }
                    }
                }
                previousMessageCount = currentMessageList.size // Actualizar el contador
                // --- FIN LÓGICA DE NOTIFICACIÓN ---

                _messages.value = currentMessageList
                fetchUserDetailsIfNeeded(currentMessageList) // Esto ya lo tenías
            }
            .catch { exception -> /* ... (sin cambios) ... */ }
            .launchIn(viewModelScope)
    }

    // --- NUEVO: Función para buscar detalles de usuarios ---



    fun clearError() {
        _error.value = null
    }
}