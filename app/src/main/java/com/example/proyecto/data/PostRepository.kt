package com.example.proyecto.data

import android.util.Log
import com.example.proyecto.utils.model.LugarTuristico
import com.example.proyecto.utils.model.Ubicacion
import com.google.firebase.database.*

fun obtenerLugaresDesdeFirebase(
    onSuccess: (List<LugarTuristico>) -> Unit,
    onError: (String) -> Unit
) {
    val database = FirebaseDatabase.getInstance().reference.child("publicaciones")

    database.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val lugares = mutableListOf<LugarTuristico>()
            for (postSnapshot in snapshot.children) {
                try {
                    val nombre = postSnapshot.child("userId").getValue(String::class.java) ?: ""
                    val descripcion = postSnapshot.child("descripcion").getValue(String::class.java) ?: ""
                    val imagenUrl = postSnapshot.child("imagenUrl").getValue(String::class.java) ?: ""
                    val lat = postSnapshot.child("posicion/latitude").getValue(Double::class.java) ?: 0.0
                    val lon = postSnapshot.child("posicion/longitude").getValue(Double::class.java) ?: 0.0

                    lugares.add(
                        LugarTuristico(
                            nombre,
                            imagenUrl,
                            descripcion,
                            Ubicacion(lat, lon)
                        )
                    )
                } catch (e: Exception) {
                    Log.e("Firebase", "Error al parsear: ${e.message}")
                }
            }
            onSuccess(lugares)
        }

        override fun onCancelled(error: DatabaseError) {
            onError(error.message)
        }
    })
}