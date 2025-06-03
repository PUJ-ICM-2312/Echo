package com.example.proyecto.utils.model


data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val cedula: String = "",
    val username: String = "",
    val correoElectronico: String = "",
    val fotoDePerfilUrl: String = "",
    val telefono: String = "",
    val conectado: Boolean = false,
    val latitud: Ubicacion? = null,
    val amigos: Map<String, Amigo> = emptyMap(),
    val sentRequests : Map<String, Usuario> = emptyMap(),
    val receivedRequests : Map<String, Usuario> = emptyMap()
)
