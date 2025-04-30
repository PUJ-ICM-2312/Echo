package com.example.proyecto.ui.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.proyecto.data.obtenerLugaresDesdeFirebase
import com.example.proyecto.ui.components.SharedScaffold
import com.example.proyecto.utils.LocationHelper
import com.example.proyecto.utils.model.LugarTuristico
import com.example.proyecto.utils.model.Ubicacion
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

@Composable
fun EchoScreen(onTabSelected: (Int) -> Unit, navController: NavController, onPlaceSelected: (location: String, latitude: Double, longitude: Double) -> Unit) {

    val lugaresState = remember { mutableStateOf<List<LugarTuristico>>(emptyList()) }
    var places by remember { mutableStateOf<List<LugarTuristico>>(emptyList()) }
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    var username by remember { mutableStateOf("") }



    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect

        val db = FirebaseDatabase.getInstance().getReference("usuarios").child(userId)

        db.child("username").get()
            .addOnSuccessListener { snapshot ->
                username = snapshot.getValue(String::class.java) ?: "Usuario desconocido"
                Log.d("EchoScreen", "Username: $username")
            }
            .addOnFailureListener { e ->
                Log.e("EchoScreen", "Error al obtener el username: $e")
            }


        // Obtener lugares desde Firebase
        obtenerLugaresDesdeFirebase(
            onSuccess = { lugares ->
                val lugaresCombinados = lugares + places
                lugaresState.value = lugaresCombinados

            },
            onError = {
                Log.e("EchoScreen", "Error: $it")
            }
        )
    }

    places = listOf(
        LugarTuristico(
            "Monserrate",
            "https://bogota.gov.co/sites/default/files/styles/1050px/public/2023-01/iglesia-monserrate-1.jpg",
            "La icónica montaña con vistas panorámicas de Bogotá.",
            Ubicacion(4.6051, -74.0551)
        ),
        LugarTuristico(
            "La Candelaria",
            "https://i.revistalternativa.com/cms/2023/11/18113937/La-Candelaria.jpg?r=1_1",
            "El corazón histórico de la ciudad, con calles coloridas y cultura.",
            Ubicacion(4.5981, -74.0721)
        ),
        LugarTuristico(
            "Parque Simón Bolívar",
            "https://images.adsttc.com/media/images/5c17/0d02/08a5/e516/a300/006b/newsletter/Bargut_nueva.jpg?1545014498",
            "El pulmón verde de Bogotá, ideal para caminar y hacer deporte.",
            Ubicacion(4.6584, -74.0935)
        ),
        LugarTuristico(
            "Museo del Oro",
            "https://d3nmwx7scpuzgc.cloudfront.net/sites/default/files/media/image/museo-del-oro-mo-salas-exposicion-permanente-2022-640x400.jpg",
            "Uno de los museos más importantes de Colombia, con piezas prehispánicas.",
            Ubicacion(4.6010, -74.0727)
        ),
        LugarTuristico(
            "Plaza de Bolívar",
            "https://cdn.colombia.com/sdi/2013/11/27/plaza-de-bolivar-714091.jpg",
            "El centro político y cultural de la ciudad.",
            Ubicacion(4.5981, -74.0760)
        ),
        LugarTuristico(
            "Zona T",
            "https://cloudfront-us-east-1.images.arcpublishing.com/infobae/T6XMHHZHLNEYPHTAG7HG6R3OD4.jpeg",
            "Una de las mejores zonas para la vida nocturna y gastronomía.",
            Ubicacion(4.6673, -74.0532)
        ),
        LugarTuristico(
            "Jardín Botánico",
            "https://images.adsttc.com/media/images/6080/d60e/e6cf/df01/64fc/4ad4/newsletter/dsc4524.jpg?1619056204",
            "Un espacio natural con gran diversidad de flora.",
            Ubicacion(4.6606, -74.0963)
        ),
        LugarTuristico(
            "Usaquén",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjP9Azz6r6CqVlAI0AZvc7PCnDUWcp5rex_g&s",
            "Un barrio tradicional con mercados artesanales y restaurantes.",
            Ubicacion(4.7026, -74.0345)
        ),
        LugarTuristico(
            "Chorro de Quevedo",
            "https://images.hive.blog/p/8DAuGnTQCLptZgjHUrRAJGcW4y1D4A5QVJJ7zjzqqKdfVHSS6NapSCCAhET8AGStKpbEh72YGjcyDAVeCetw8EbCUpCJcxmXkUnekNHLCS8X66abRwkQJH7LP7kByG3DUfLC3nESxEniuVyX92oRHwXjnyZKturEEHR427sH54A?format=match&mode=fit",
            "Lugar emblemático con grafitis y cultura callejera.",
            Ubicacion(4.5964, -74.0722)
        ),
        LugarTuristico(
            "Maloka",
            "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRPh_PNIG1HuZSJVa4RADLdFRL-XUAGG2UCOA&s",
            "Centro interactivo de ciencia y tecnología.",
            Ubicacion(4.6425, -74.1115)
        )
    )



    val users = listOf("JuanPerez", "AnaGomez", "CarlosRod", "LuisaF", "SantiagoM", "ValeriaP")

    SharedScaffold(selectedTab = 1, onTabSelected = onTabSelected, navController = navController) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(lugaresState.value) { (location, imageUrl, caption,coords) ->
                val user = users.random() // Nombre de usuario aleatorio

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable {
                            onPlaceSelected(location, coords.latitude, coords.longitude)
                        },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                )
                {
                    Column(modifier = Modifier.padding(16.dp)) {


                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Usuario",
                                modifier = Modifier.size(40.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = username,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Ubicación",
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = locationHelper.getAddressFromLatLng(coords.latitude, coords.longitude) ?: "Dirección no disponible",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))


                        AsyncImage(
                            model = imageUrl,
                            contentDescription = "Imagen de $location",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.height(8.dp))


                        Text(
                            text = caption,
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}
