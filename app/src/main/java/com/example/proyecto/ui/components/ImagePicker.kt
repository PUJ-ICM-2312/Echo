package com.example.proyecto.ui.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePicker(imageType: String, onUploadSuccess: (String) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var uploading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(true) }
    val auth = FirebaseAuth.getInstance()
    val database = Firebase.database.reference
    val storage = FirebaseStorage.getInstance()

    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            coroutineScope.launch {
                uploading = true

                val baos = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                val data = baos.toByteArray()

                val user = auth.currentUser
                user?.let {
                    // Aquí cambia el tipo de carpeta según el parámetro imageType
                    val imageRef = if (imageType == "post") {
                        storage.reference.child("posts/${user.uid}_${System.currentTimeMillis()}.jpg")
                    } else {
                        storage.reference.child("fotosDePerfil/${user.uid}.jpg")
                    }

                    val uploadTask = imageRef.putBytes(data)

                    uploadTask.addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()
                            val userRef = database.child("usuarios").child(user.uid)
                            // Almacena la URL en la base de datos dependiendo del tipo de imagen
                            if (imageType == "post") {
                                userRef.child("posts").push().setValue(downloadUrl)
                            } else {
                                userRef.child("fotoDePerfilUrl").setValue(downloadUrl)
                            }

                            uploading = false
                            onUploadSuccess(downloadUrl)
                        }
                    }.addOnFailureListener {
                        uploading = false
                    }
                }
            }
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                uploading = true

                val user = auth.currentUser
                val storageRef = storage.reference
                // Aquí también cambia según el tipo de imagen (post o perfil)
                val imageRef = if (imageType == "post") {
                    storageRef.child("posts/${user?.uid}_${System.currentTimeMillis()}.jpg")
                } else {
                    storageRef.child("fotosDePerfil/${user?.uid}.jpg")
                }

                val uploadTask = imageRef.putFile(it)

                uploadTask.addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        val downloadUrl = downloadUri.toString()
                        val userRef = database.child("usuarios").child(user!!.uid)
                        if (imageType == "post") {
                            userRef.child("posts").push().setValue(downloadUrl)
                        } else {
                            userRef.child("fotoDePerfilUrl").setValue(downloadUrl)
                        }

                        uploading = false
                        onUploadSuccess(downloadUrl)
                    }
                }.addOnFailureListener {
                    uploading = false
                }
            }
        }
    }

    val requestCameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(intent)
        } else {
            // Permiso denegado
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Seleccionar Imagen") },
            text = { Text("¿Qué quieres hacer?") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    val permission = Manifest.permission.CAMERA
                    if (ContextCompat.checkSelfPermission(context, permission) != PermissionChecker.PERMISSION_GRANTED) {
                        requestCameraPermissionLauncher.launch(permission)
                    } else {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        takePictureLauncher.launch(intent)
                    }
                }) {
                    Text("Tomar Foto")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showDialog = false
                    pickImageLauncher.launch("image/*")
                }) {
                    Text("Seleccionar de Galería")
                }
            }
        )
    }

    if (uploading) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            CircularProgressIndicator()
        }
    }
}
