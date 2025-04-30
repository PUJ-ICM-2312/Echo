package com.example.proyecto.ui.components

import android.app.Activity
import android.content.Intent
import android.provider.MediaStore
import androidx.compose.foundation.background
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.navigation.NavController
import com.example.proyecto.navigation.Screen
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import android.Manifest
import android.net.Uri



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedScaffold(navController: NavController,
                   selectedTab: Int?=null, onTabSelected: (Int) -> Unit={},
                   content: @Composable () -> Unit,
)  {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid
    var isUploading by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
    var postPictureUrl by remember { mutableStateOf("") }
    val database = Firebase.database.reference
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap

            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference

            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser

            currentUser?.let { user ->
                val imageRef = storageRef.child("fotosDePerfil/${user.uid}.jpg")

                isUploading = true
                val uploadTask = imageRef.putBytes(data)
                uploadTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        val database = Firebase.database.reference
                        val userRef = database.child("usuarios").child(user.uid)
                        userRef.child("fotoDePerfilUrl").setValue(downloadUrl)
                        isUploading = false
                    }
                }.addOnFailureListener { e ->
                    isUploading = false
                    // Puedes mostrar un snackbar o alert aquí si quieres
                }
            }
        }
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(takePictureIntent)
        } else {
            // Permiso denegado, puedes mostrar algo aquí si quieres
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    text = "ECHO",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1E88E5),
                titleContentColor = Color.White
            )
        )


        if(selectedTab!=null){
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1E88E5),
                contentColor = Color.White,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { onTabSelected(0) },
                    text = { Text("MAPA") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { onTabSelected(1) },
                    text = { Text("FEED") }
                )
            }
        }


        Box(modifier = Modifier.weight(1f)) {
            content()
        }


        NavigationBar(modifier = Modifier.fillMaxWidth()) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Mapa") },
                selected = false,
                onClick = { onTabSelected(0) }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Favorite, contentDescription = "Feed") },
                selected = false,
                onClick = { onTabSelected(1) }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Add, contentDescription = "Tomar Foto") },
                selected = false,
                onClick = {
                    showImagePicker = true
                }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                selected = false,

                onClick = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
    if (showImagePicker) {
        ImagePicker(imageType = "post") { downloadUrl ->
            postPictureUrl = downloadUrl
            showImagePicker = false

            // Redirigir a otra pantalla pasando la URL como argumento
            val encodedUrl = Uri.encode(downloadUrl)
            navController.navigate("NewPost/$encodedUrl")
        }
    }

}

