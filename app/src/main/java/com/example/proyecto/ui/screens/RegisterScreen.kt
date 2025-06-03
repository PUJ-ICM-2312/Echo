package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyecto.navigation.Screen
import com.example.proyecto.utils.getCurrentLocation
import com.example.proyecto.utils.model.Amigo
import com.example.proyecto.utils.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import com.example.proyecto.utils.model.Ubicacion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val auth: FirebaseAuth = Firebase.auth
    val database = Firebase.database.reference
    var mail by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E88E5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "ECHO",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Registro",
                fontSize = 35.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = mail,
                onValueChange = { mail = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedContainerColor = Color.White,
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {

                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            val location = getCurrentLocation(context)
                            auth.createUserWithEmailAndPassword(mail, pass)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val uid = auth.currentUser!!.uid

                                        val geoPoint = location?.let { Ubicacion(it.latitude, location.longitude) }

                                        val usuario = Usuario(
                                            uid = uid,
                                            nombre = name,
                                            cedula = "",
                                            username = "",
                                            correoElectronico = mail,
                                            fotoDePerfilUrl = "",
                                            telefono = "",
                                            conectado = false,
                                            latitud = location?.let { Ubicacion(it.latitude, it.longitude) },
                                            amigos = emptyMap()
                                        )


                                        database.child("usuarios")
                                            .child(uid)
                                            .setValue(usuario)
                                            .addOnSuccessListener {

                                                navController.navigate(Screen.ProfileSetup.route) {
                                                    popUpTo(Screen.Register.route) { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener { e ->
                                                errorMessage = "Error al guardar datos del usuario: ${e.message}"
                                            }
                                    } else {
                                        errorMessage = task.exception?.message
                                    }
                                }
                        } catch (e: Exception) {
                            errorMessage = e.localizedMessage
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Crear cuenta", fontSize = 18.sp)
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red)
            }
        }
    }
}
