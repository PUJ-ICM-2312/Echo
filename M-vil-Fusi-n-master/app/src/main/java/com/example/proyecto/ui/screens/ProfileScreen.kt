package com.example.proyecto.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
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
import com.example.proyecto.navigation.Screen
import com.example.proyecto.ui.components.ImagePicker
import com.example.proyecto.ui.components.SharedScaffold
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid
    val database = Firebase.database.reference

    var nombreReal by remember { mutableStateOf("Cargando...") }
    var username by remember { mutableStateOf("Cargando...") }
    var profilePictureUrl by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf(currentUser?.email ?: "") }

    var showImagePicker by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var tempNombreReal by remember { mutableStateOf("") }
    var tempUsername by remember { mutableStateOf("") }
    var tempEmail by remember { mutableStateOf("") }

    LaunchedEffect(userId) {
        userId?.let { uid ->
            val userRef = database.child("usuarios").child(uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        nombreReal = snapshot.child("nombreReal").getValue(String::class.java)
                            ?: "Usuario Anónimo"
                        username = snapshot.child("username").getValue(String::class.java)
                            ?: "Sin username"
                        profilePictureUrl =
                            snapshot.child("fotoDePerfilUrl").getValue(String::class.java) ?: ""
                        userEmail = snapshot.child("correoElectronico").getValue(String::class.java)
                            ?: currentUser?.email.orEmpty()
                    } else {
                        nombreReal = "Usuario no encontrado"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    nombreReal = "Error al cargar el perfil"
                }
            })
        }
    }

    SharedScaffold(selectedTab = null, navController = navController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            if (profilePictureUrl.isNotEmpty()) {
                AsyncImage(
                    model = profilePictureUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { showImagePicker = true },
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_profile_placeholder),
                    error = painterResource(id = R.drawable.ic_profile_placeholder)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_profile_placeholder),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { showImagePicker = true },
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isEditing) {
                OutlinedTextField(
                    value = tempNombreReal,
                    onValueChange = { tempNombreReal = it },
                    label = { Text("Nombre real") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tempUsername,
                    onValueChange = { tempUsername = it },
                    label = { Text("Username (@usuario)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tempEmail,
                    onValueChange = { tempEmail = it },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth()
                )

            } else {
                Text(
                    text = nombreReal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Black
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
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isEditing) {
                        val updates = mutableMapOf<String, Any>(
                            "nombreReal" to tempNombreReal,
                            "username" to tempUsername,
                            "correoElectronico" to tempEmail
                        )

                        userId?.let { uid ->
                            val userRef = database.child("usuarios").child(uid)
                            userRef.updateChildren(updates)
                        }

                        nombreReal = tempNombreReal
                        username = tempUsername
                        userEmail = tempEmail
                    } else {
                        tempNombreReal = nombreReal
                        tempUsername = username
                        tempEmail = userEmail
                    }
                    isEditing = !isEditing
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Edit, contentDescription = if (isEditing) "Guardar" else "Editar")
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditing) "Guardar Cambios" else "Editar Perfil")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cerrar Sesión", color = Color.White)
            }
        }
    }
    if (showImagePicker) {
        ImagePicker(imageType = "perfil") { downloadUrl ->
            profilePictureUrl = downloadUrl
            showImagePicker = false

            userId?.let { uid ->
                val updates = mapOf<String, Any>(
                    "fotoDePerfilUrl" to downloadUrl
                )
                val userRef = database.child("usuarios").child(uid)
                userRef.updateChildren(updates)
            }
        }
    }
}
