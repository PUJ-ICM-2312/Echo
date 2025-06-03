package com.example.proyecto.utils.model // Asegúrate que el paquete sea correcto

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date // Usamos Date con @ServerTimestamp

data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
) {

    constructor() : this("", "", "", "", null)

}