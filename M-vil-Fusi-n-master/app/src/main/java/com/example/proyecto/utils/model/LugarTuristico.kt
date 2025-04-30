package com.example.proyecto.utils.model

data class LugarTuristico(
    val nombre: String,
    val imagenUrl: String,
    val descripcion: String,
    val posicion: Ubicacion = Ubicacion()
)
