package com.example.proyecto.utils.model

data class Amigo(
    var email: String = "",
    var uid:   String = "",
    var grupo: String = "general"
){
    constructor(): this("", "", "general")
}
