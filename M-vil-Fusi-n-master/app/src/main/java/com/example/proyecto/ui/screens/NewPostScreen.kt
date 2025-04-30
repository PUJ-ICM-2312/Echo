package com.example.proyecto.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.proyecto.ui.components.SharedScaffold
import com.example.proyecto.utils.model.LugarTuristico
import com.example.proyecto.utils.model.Ubicacion
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.type.LatLng
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NewPostScreen(imageUrl: String, navController: NavController) {
    var description by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    LaunchedEffect(Unit) {
        permissionState.launchPermissionRequest()
    }

    SharedScaffold(selectedTab = null, navController = navController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nueva publicación",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF1E88E5)
            )

            Card(
                modifier = Modifier.fillMaxWidth(0.9f),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Imagen del post",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                )
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(0.9f),
                singleLine = false,
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF1E88E5),
                    focusedLabelColor = Color(0xFF1E88E5),
                )
            )

            Button(
                onClick = {
                    scope.launch {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                        val ubicacion = obtenerUbicacionActual(context)
                        val post = LugarTuristico(
                            nombre = userId,
                            descripcion = description,
                            imagenUrl = imageUrl,
                            posicion = ubicacion ?: Ubicacion()
                        )

                        guardarPostEnRealtimeDB(
                            post = post,
                            onSuccess = { navController.popBackStack() },
                            onError = { e -> Log.e("NewPostScreen", "Error al guardar", e) }
                        )
                    }
                },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E88E5),
                    contentColor = Color.White
                )
            ) {
                Text("Publicar")
            }
        }
    }
}
fun guardarPostEnRealtimeDB(post: LugarTuristico, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
    val database = FirebaseDatabase.getInstance().reference
    val nuevaPublicacionRef = database.child("publicaciones").push()

    nuevaPublicacionRef.setValue(post)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onError(e) }
}
suspend fun obtenerUbicacionActual(context: Context): Ubicacion? {
    val locationProvider = LocationServices.getFusedLocationProviderClient(context)
    return suspendCancellableCoroutine { cont ->
        try {
            locationProvider.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                    cont.resume(Ubicacion(lat, lon))
                } else {
                    cont.resume(null)
                }
            }.addOnFailureListener {
                cont.resume(null)
            }
        } catch (e: Exception) {
            cont.resume(null)
        }
    }
}



