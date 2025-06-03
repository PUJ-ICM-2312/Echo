package com.example.proyecto.utils.model

data class LugarTuristico(
    val userId: String,
    val imagenUrl: String,
    val descripcion: String,
    val posicion: Ubicacion = Ubicacion()
)
