package com.example.proyecto.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.proyecto.R
import com.example.proyecto.data.AmigosRepository
import com.example.proyecto.navigation.Screen
import com.example.proyecto.ui.components.obtenerUsernamePorUid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendRequestScreen(
    database: DatabaseReference = FirebaseDatabase.getInstance().reference , navController: NavController
) {
    var navController2 = navController
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    if (currentUserId == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "No autenticado",
                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Usuario no autenticado",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        return
    }

    var solicitudes by remember { mutableStateOf<List<String>>(emptyList()) }

    // 1) Escuchar solicitudes de amistad en Firebase
    LaunchedEffect(currentUserId) {
        val receivedRef = database
            .child("usuarios")
            .child(currentUserId)
            .child("receivedRequests")

        receivedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.key }
                solicitudes = lista
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Error al cargar solicitudes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 2) Scaffold con TopAppBar
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes de Amistad") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFF1E88E5),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )

            )
        }
    ) { scaffoldPadding ->
        // 3) Contenido principal, aunque esté vacío o con items
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Si no hay solicitudes, mostramos un mensaje centrado
            if (solicitudes.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Ícono “sin solicitudes”
                    Icon(
                        painter = rememberVectorPainter(image = Icons.Default.Notifications),
                        contentDescription = "Sin solicitudes",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No tienes solicitudes pendientes",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontStyle = FontStyle.Italic
                        ),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Si hay solicitudes, las listamos en tarjetas
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(solicitudes) { idSolicitante ->
                        FriendRequestItem(
                            solicitanteId = idSolicitante,
                            onAccept = {
                                coroutineScope.launch {
                                    AmigosRepository().aceptarSolicitudAmistad(
                                        de = idSolicitante,
                                        para = currentUserId
                                    )
                                }
                            },
                            onReject = {
                                AmigosRepository().rechazarSolicitudAmistad(
                                    de = idSolicitante,
                                    para = currentUserId
                                )
                            },
                            navController
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun FriendRequestItem(
    solicitanteId: String,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    navController: NavController
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = elevatedCardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            var username by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(solicitanteId) {
                obtenerUsernamePorUid(solicitanteId) { result ->
                    username = result
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Aquí puedes reemplazar el icono por la foto de perfil si la tienes
                Icon(
                    painter = rememberVectorPainter(image = Icons.Default.AccountBox),
                    contentDescription = "Avatar invitado",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape).clickable {
                            navController.navigate(Screen.FriendProfile.createRoute(solicitanteId))

                        }
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "@Username: $username",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

            }

            Spacer(Modifier.height(12.dp))

            // Botones de Aceptar / Rechazar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "Aceptar", color = Color.White)
                }

                Spacer(Modifier.width(16.dp))

                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(text = "Rechazar", color = Color.White)
                }
            }
        }
    }
}