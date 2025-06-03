package com.example.proyecto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.proyecto.navigation.Screen
import com.example.proyecto.utils.getCurrentLocation
import com.example.proyecto.utils.model.Ubicacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogInScreen(navController: NavController) {
    val auth: FirebaseAuth = Firebase.auth
    var mail by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val database = Firebase.database.reference


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
                text = "Inicio de sesión",
                fontSize = 35.sp,
                color = Color.White
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
                    focusedLabelColor = Color.White,
                    //focusedBorderColor = Color.White,
                    //unfocusedBorderColor = Color.LightGray,
                    //containerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.White,
                    unfocusedIndicatorColor = Color.LightGray,
                    focusedLabelColor = Color.White,
                    //focusedBorderColor = Color.White,
                    //unfocusedBorderColor = Color.LightGray,
                    //containerColor = Color.White
                )
            )
            Text(
                text = "Aún no tienes cuenta? Regístrate",
                color = Color.White,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { navController.navigate(Screen.Register.route) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    CoroutineScope(Dispatchers.Main).launch {
                        try {
                            auth.signInWithEmailAndPassword(mail, pass)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                                        // Obtener ubicación y actualizar en RealtimeDatabase
                                        CoroutineScope(Dispatchers.Main).launch {
                                            val location= getCurrentLocation(context)
                                            val lat = location?.latitude ?: 0.0
                                            val lng = location?.longitude ?: 0.0

                                            val updates = mapOf(
                                                "latitud" to Ubicacion(lat,lng),
                                                "conectado" to true
                                            )

                                            database.child("usuarios").child(uid)
                                                .updateChildren(updates)
                                                .addOnSuccessListener {
                                                    navController.navigate(Screen.Home.route) {
                                                        popUpTo(Screen.Login.route) { inclusive = true }
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    errorMessage = "Error al subir ubicación: ${e.message}"
                                                }
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
            ) {
            Text("Iniciar sesión", fontSize = 18.sp)
        }


            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = Color.Red)
            }
        }
    }
}
