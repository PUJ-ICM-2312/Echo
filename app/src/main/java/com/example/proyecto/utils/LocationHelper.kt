package com.example.proyecto.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import com.example.proyecto.utils.model.Ubicacion
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
import java.util.Locale

class LocationHelper(private val context: Context) {

    internal val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Registra actualizaciones periódicas de ubicación y llama al callback con la ubicación LatLng.
     * Devuelve el LocationCallback para poder detener las actualizaciones cuando se quiera.
     */
    @SuppressLint("MissingPermission") // Asegúrate de manejar permisos antes de llamar esta función
    fun registerLocationUpdates(callback: (LatLng) -> Unit): LocationCallback {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000 // intervalo 5 segundos
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    callback(LatLng(location.latitude, location.longitude))
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        return locationCallback
    }

    /**
     * Convierte una dirección a coordenadas LatLng
     */
    fun getLatLngFromAddress(address: String): Pair<Double, Double>? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses: List<Address> = geocoder.getFromLocationName(address, 1) ?: emptyList()
            if (addresses.isNotEmpty()) {
                val location = addresses[0]
                Pair(location.latitude, location.longitude)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte coordenadas LatLng a dirección en texto
     */
    fun getAddressFromLatLng(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
            if (addresses.isNotEmpty()) {
                addresses[0].getAddressLine(0)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Obtiene la ubicación actual del usuario una sola vez de forma asíncrona.
 */
suspend fun getCurrentLocation(context: Context): Location? {
    return try {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient.lastLocation.await()
    } catch (e: SecurityException) {
        Log.e("Location", "Permiso denegado", e)
        null
    } catch (e: Exception) {
        Log.e("Location", "Error al obtener ubicación", e)
        null
    }
}

/**
 * Inicia el rastreo de la ubicación del usuario, actualizando la posición en Firestore cada vez que cambia.
 * Devuelve una función para detener el rastreo y marcar al usuario como desconectado.
 */
@SuppressLint("MissingPermission") // Asegúrate de manejar permisos antes de llamar esta función
fun startTrackingUserLocation(
    context: Context,
    userId: String,
    coroutineScope: CoroutineScope,
    onLocationUpdated: ((Location) -> Unit)? = null
): () -> Unit {

    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val databaseRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId)

    val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        10_000L // cada 10 segundos
    ).apply {
        setMinUpdateDistanceMeters(10f)
        setWaitForAccurateLocation(true)
    }.build()

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return

            val updates = mapOf(
                "latitud" to Ubicacion(location.latitude,location.longitude),
                "conectado" to true
            )

            databaseRef.updateChildren(updates)
                .addOnFailureListener {
                    Log.e("Location", "Error subiendo ubicación", it)
                }

            onLocationUpdated?.invoke(location)
        }
    }

    try {
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    } catch (e: SecurityException) {
        Log.e("Location", "Permisos no concedidos", e)
    }

    // Devuelve la función para detener el rastreo
    return {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        databaseRef.child("conectado").setValue(false)
    }
}

