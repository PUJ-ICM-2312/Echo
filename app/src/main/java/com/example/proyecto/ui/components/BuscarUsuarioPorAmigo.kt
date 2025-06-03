package com.example.proyecto.ui.components

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlin.jvm.java

fun obtenerUsernamePorUid(uid: String, callback: (String?) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference
    val usuarioRef = database.child("usuarios").child(uid).child("username")

    usuarioRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val username = snapshot.getValue(String::class.java)
            callback(username) // puede ser null si no existe
        }

        override fun onCancelled(error: DatabaseError) {
            callback(null) // error al leer
        }
    })
}

