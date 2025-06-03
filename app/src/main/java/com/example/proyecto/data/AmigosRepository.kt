package com.example.proyecto.data

import com.example.proyecto.utils.model.Amigo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import kotlinx.coroutines.tasks.await

class AmigosRepository {
    private val database = FirebaseDatabase.getInstance().reference

    suspend fun agregarAmigo(usuarioId: String, amigo: Amigo) {
        val amigosRef = database
            .child("usuarios")
            .child(usuarioId)
            .child("amigos")

        amigosRef.child(amigo.uid).setValue(amigo).await()

        amigosRef.child("_init").removeValue().await()
    }
    suspend fun eliminarAmigo(usuarioId: String, amigoId: String) {
        database.child("usuarios")
            .child(usuarioId)
            .child("amigos")
            .child(amigoId)
            .removeValue()
            .await()

        database.child("usuarios")
            .child(amigoId)
            .child("amigos")
            .child(usuarioId)
            .removeValue()
            .await()
    }
    fun obtenerAmigos(usuarioId: String, callback: (List<Amigo>) -> Unit) {
        database.child("usuarios")
            .child(usuarioId)
            .child("amigos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lista = if (snapshot.exists()) {
                        snapshot.children
                            .filter { it.key != "_init" }

                            .mapNotNull { it.getValue(Amigo::class.java) }
                    } else {
                        emptyList()
                    }
                    callback(lista)
                }

                override fun onCancelled(error: DatabaseError) {

                    callback(emptyList())
                }
            })
    }
    suspend fun actualizarGrupoDeAmigo(usuarioId: String, amigoId: String, nuevoGrupo: String) {
        database.child("usuarios")
            .child(usuarioId)
            .child("amigos")
            .child(amigoId)
            .child("grupo")
            .setValue(nuevoGrupo)
            .await()
    }
    fun obtenerGruposUsuario(usuarioId: String, callback: (List<String>) -> Unit) {
        database.child("usuarios")
            .child(usuarioId)
            .child("grupos")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val listaDeGrupos = if (snapshot.exists()) {
                        snapshot.children.mapNotNull { it.key }
                    } else {
                        listOf("general")
                    }
                    callback(listaDeGrupos)
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(listOf("general"))
                }
            })
    }
    suspend fun agregarGrupoUsuario(usuarioId: String, nombreGrupo: String) {
        database.child("usuarios")
            .child(usuarioId)
            .child("grupos")
            .child(nombreGrupo)
            .setValue(true)
            .await()
    }
    suspend fun agregarAmigoMutuo(
        currentUid: String,
        currentEmail: String,
        friendUid: String,
        friendEmail: String
    ) {

        val amigoParaB = Amigo(
            uid   = friendUid,
            email = friendEmail,
            grupo = "general"
        )

        val amigoParaA = Amigo(
            uid   = currentUid,
            email = currentEmail,
            grupo = "general"
        )

        agregarAmigo(currentUid, amigoParaB)
        agregarAmigo(friendUid, amigoParaA)
    }

    fun enviarSolicitudAmistad(de: String, para: String) {

        // Añadir 'para' a 'sentRequests' de 'de'
        database.child("usuarios").child(de).child("sentRequests")
            .child(para).setValue(true)

        // Añadir 'de' a 'receivedRequests' de 'para'
        database.child("usuarios").child(para).child("receivedRequests")
            .child(de).setValue(true)
    }

    suspend fun aceptarSolicitudAmistad(de: String, para: String) {
        val database = FirebaseDatabase.getInstance().reference
        val usuariosRef = database.child("usuarios")

        // Obtener los datos del usuario "de"
        val amigoDeSnapshot = usuariosRef.child(de).get().await()
        val amigoParaSnapshot = usuariosRef.child(para).get().await()

        val amigoDe = amigoDeSnapshot.getValue(Amigo::class.java)
        val amigoPara = amigoParaSnapshot.getValue(Amigo::class.java)

        if (amigoDe != null && amigoPara != null) {
            // Guardar a ambos como amigos
            usuariosRef.child(de).child("amigos").child(para).setValue(amigoPara).await()
            usuariosRef.child(para).child("amigos").child(de).setValue(amigoDe).await()

            // Eliminar la solicitud de amistad
            usuariosRef.child(de).child("receivedRequests").child(para).removeValue().await()
            usuariosRef.child(para).child("sentRequests").child(de).removeValue().await()

            // Eliminar marcador de inicialización si existe
            usuariosRef.child(de).child("amigos").child("_init").removeValue().await()
            usuariosRef.child(para).child("amigos").child("_init").removeValue().await()
        }
    }


    fun rechazarSolicitudAmistad(de: String, para: String) {
        val database = FirebaseDatabase.getInstance().reference

        // Eliminar la solicitud sin agregar como amigo
        database.child("usuarios").child(de).child("receivedRequests").child(para).removeValue()
        database.child("usuarios").child(para).child("sentRequests").child(de).removeValue()
    }



}
