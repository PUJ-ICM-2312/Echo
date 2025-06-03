package com.example.proyecto.utils

import com.example.proyecto.utils.model.UserLocation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

fun observeConnectedUsers(onUsersUpdated: (List<UserLocation>) -> Unit): ListenerRegistration {
    return Firebase.firestore.collection("usuarios")
        .whereEqualTo("conectado", true)
        .limit(100)
        .addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val users = snapshot.documents.mapNotNull { doc ->
                val uid = doc.id
                val geopoint = doc.getGeoPoint("latitud") // asumiendo que el GeoPoint está guardado bajo la clave "latitud"
                val name = doc.getString("nombre")
                if (geopoint != null && name != null) {
                    UserLocation(uid,name, LatLng(geopoint.latitude, geopoint.longitude))
                } else null
            }
            onUsersUpdated(users)
        }
}
