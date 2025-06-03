package com.example.proyecto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.proyecto.R
import com.example.proyecto.data.AmigosRepository
import com.example.proyecto.utils.model.Amigo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(
    friendUid: String,
    navController: NavController
) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val currentUid = currentUser?.uid ?: return
    val currentEmail = currentUser.email ?: ""
    val database = Firebase.database.reference

    // ─── Estados para la info del “amigo” ───
    var nombreReal by remember { mutableStateOf("Cargando...") }
    var username by remember { mutableStateOf("Cargando...") }
    var profilePictureUrl by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") } // correo del amigo

    // ─── Estado para saber si “ya somos amigos” ───
    var isFriend by remember { mutableStateOf(false) }
    var solicitudPendiente by remember { mutableStateOf(false) }

    // ─── Cargar datos del amigo (solo lectura) ───
    LaunchedEffect(friendUid) {
        // 1) Obtener sus datos en /usuarios/{friendUid}
        database.child("usuarios")
            .child(friendUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        nombreReal = snapshot.child("nombre").getValue(String::class.java) ?: "Sin nombre"
                        username = snapshot.child("username").getValue(String::class.java) ?: "Sin username"
                        profilePictureUrl = snapshot.child("fotoDePerfilUrl").getValue(String::class.java) ?: ""
                        userEmail = snapshot.child("correoElectronico").getValue(String::class.java) ?: ""
                    } else {
                        nombreReal = "Usuario no encontrado"
                        username = ""
                        profilePictureUrl = ""
                        userEmail = ""
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    nombreReal = "Error cargando perfil"
                }
            })

        // 2) Revisar si en /usuarios/{currentUid}/amigos/{friendUid} existe
        database.child("usuarios")
            .child(currentUid)
            .child("amigos")
            .child(friendUid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isFriend = snapshot.exists()
                    // 3) Revisar si ya se envió una solicitud de amistad
                    database.child("sentRequests")
                        .child(friendUid) // solicitudes pendientes que tiene ese amigo
                        .child(currentUid) // nuestra solicitud
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                solicitudPendiente = snapshot.exists()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                solicitudPendiente = false
                            }
                        })

                }
                override fun onCancelled(error: DatabaseError) {
                    isFriend = false
                }
            })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (username.isNotBlank()) "@$username" else "Perfil de Amigo") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Atrás"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // FOTO DE PERFIL
            if (profilePictureUrl.isNotEmpty()) {
                AsyncImage(
                    model = profilePictureUrl,
                    contentDescription = "Foto de perfil de $nombreReal",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_profile_placeholder),
                    error = painterResource(id = R.drawable.ic_profile_placeholder)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile_placeholder),
                    contentDescription = "Foto de perfil de $nombreReal",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // NOMBRE REAL / USERNAME
            Text(
                text = nombreReal,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "@$username",
                fontSize = 18.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = userEmail,
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ─── BOTÓN “AÑADIR / ELIMINAR AMIGO” ───
            val buttonText = when {
                isFriend -> "Eliminar Amigo"
                solicitudPendiente -> "Solicitud pendiente"
                else -> "Enviar Solicitud de Amistad"
            }

            val buttonEnabled = !solicitudPendiente || isFriend

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            if (isFriend) {
                                AmigosRepository().eliminarAmigo(currentUid, friendUid)
                                isFriend = false
                            } else if (!solicitudPendiente) {
                                AmigosRepository().enviarSolicitudAmistad(
                                    de = currentUid,
                                    para = friendUid
                                )
                                solicitudPendiente = true
                            }
                        } catch (e: Exception) {
                            // Opcional: mostrar un Toast
                        }
                    }
                },
                enabled = buttonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        isFriend -> Color.LightGray
                        solicitudPendiente -> Color.Gray
                        else -> MaterialTheme.colorScheme.primary
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = buttonText,
                    color = when {
                        isFriend -> Color.Black
                        solicitudPendiente -> Color.White
                        else -> Color.White
                    }
                )
            }

        }
    }
}
