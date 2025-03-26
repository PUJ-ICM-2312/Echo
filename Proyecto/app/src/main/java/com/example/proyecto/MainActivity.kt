package com.example.proyecto

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import android.net.Uri
import android.provider.MediaStore
import androidx.camera.core.Camera
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavigationStack()
                }
            }
        }
    }
}


@Composable
fun NavigationStack() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(route = Screen.Login.route) {
            LogInScreen(navController = navController)
        }
        composable(route = Screen.Register.route) { // Agregar la pantalla de registro
            RegisterScreen(navController = navController)
        }
        composable(route = Screen.Home.route) {
            when (selectedTab) {
                0 -> mapScreen(onTabSelected = { selectedTab = it })
                1 -> EchoScreen(onTabSelected = { selectedTab = it })
            }
        }
        composable(route = Screen.ProfileSetup.route) {
            ProfileSetupScreen(navController = navController)
        }
    }

}


sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
    object Register : Screen("register_screen")
    object ProfileSetup : Screen("profile_setup_screen")
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogInScreen(navController: NavController){
        var mail by remember { mutableStateOf("") }
        var pass by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E88E5)),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ){
            Text(
                text="ECHO",
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
            Spacer(modifier=Modifier.height(16.dp))
            TextField(
                value = mail,
                onValueChange = { mail = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    containerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    containerColor = Color.White
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
                onClick = { navController.navigate(Screen.Home.route) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Verde
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Iniciar sesión", fontSize = 18.sp)
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController){
    var mail by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E88E5)),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ){
            Text(
                text="ECHO",
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
            Spacer(modifier=Modifier.height(16.dp))
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    containerColor = Color.White
                )
            )
            Spacer(modifier=Modifier.height(16.dp))
            TextField(
                value = mail,
                onValueChange = { mail = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    containerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.LightGray,
                    containerColor = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate(Screen.ProfileSetup.route) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)), // Verde
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Crear cuenta", fontSize = 18.sp)
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E88E5), Color(0xFF42A5F5))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(24.dp)
        ) {
            Text(
                text = "Configurar Perfil",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E88E5),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Imagen con sombra y animación
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
                    .border(4.dp, Color(0xFF1E88E5), CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedImageUri),
                        contentDescription = "Imagen seleccionada",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.AddCircle,
                        contentDescription = "Seleccionar imagen",
                        tint = Color(0xFF1E88E5),
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nombre de usuario") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Usuario") },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(10.dp)),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Color(0xFF1E88E5),
                    unfocusedBorderColor = Color.Gray,
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Botón Finalizar con efecto de elevación
            Button(
                onClick = { navController.navigate(Screen.Home.route) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                Text(
                    "Finalizar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedScaffold(selectedTab: Int, onTabSelected: (Int) -> Unit, content: @Composable () -> Unit) {
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as Bitmap
            // Aquí puedes manejar la imagen capturada
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Barra superior
        TopAppBar(
            title = {
                Text(
                    text = "ECHO",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1E88E5),
                titleContentColor = Color.White
            )
        )

        // Tabs de navegación
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

        // Contenido dinámico
        Box(modifier = Modifier.weight(1f)) {
            content()
        }

        // Botón flotante para abrir la cámara
        NavigationBar(modifier = Modifier.fillMaxWidth()) {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Mapa") },
                selected = false,
                onClick = {}
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Favorite, contentDescription = "Feed") },
                selected = false,
                onClick = {}
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Add, contentDescription = "Tomar Foto") },
                selected = false,
                onClick = {
                    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    takePictureLauncher.launch(takePictureIntent)
                }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                selected = false,
                onClick = {}
            )
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun mapScreen(onTabSelected: (Int) -> Unit) {
    var selectedGroup by remember { mutableStateOf("Familia") }
    val groups = listOf("Familia", "Amigos", "Trabajo")
    var expanded by remember { mutableStateOf(false) }
    var hotZonesActive by remember { mutableStateOf(false) }

    SharedScaffold(selectedTab = 0, onTabSelected = onTabSelected) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Imagen del mapa
            AsyncImage(
                model = "https://media.wired.com/photos/59269cd37034dc5f91bec0f1/master/pass/GoogleMapTA.jpg",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Controles sobre el mapa
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selector de grupo
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedGroup,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grupo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .weight(1f)
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        groups.forEach { group ->
                            DropdownMenuItem(
                                text = { Text(group) },
                                onClick = {
                                    selectedGroup = group
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Botón de zonas calientes
                IconButton(
                    onClick = { hotZonesActive = !hotZonesActive }
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder, // Usa un ícono de fuego personalizado
                        contentDescription = "Activar zonas calientes",
                        tint = if (hotZonesActive) Color.Red else Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Indicador de posición del usuario
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.Blue)
                    .align(Alignment.Center)
            )

            // Simulación de zonas calientes si están activas
            if (hotZonesActive) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.5f))
                        .align(Alignment.Center)
                )
            }
        }
    }
}



@Composable
fun EchoScreen(onTabSelected: (Int) -> Unit) {
    val posts = listOf(
        Triple("Monserrate", "https://bogota.gov.co/sites/default/files/styles/1050px/public/2023-01/iglesia-monserrate-1.jpg", "La icónica montaña con vistas panorámicas de Bogotá."),
        Triple("La Candelaria", "https://i.revistalternativa.com/cms/2023/11/18113937/La-Candelaria.jpg?r=1_1", "El corazón histórico de la ciudad, con calles coloridas y cultura."),
        Triple("Parque Simón Bolívar", "https://images.adsttc.com/media/images/5c17/0d02/08a5/e516/a300/006b/newsletter/Bargut_nueva.jpg?1545014498", "El pulmón verde de Bogotá, ideal para caminar y hacer deporte."),
        Triple("Museo del Oro", "https://d3nmwx7scpuzgc.cloudfront.net/sites/default/files/media/image/museo-del-oro-mo-salas-exposicion-permanente-2022-640x400.jpg", "Uno de los museos más importantes de Colombia, con piezas prehispánicas."),
        Triple("Plaza de Bolívar", "https://cdn.colombia.com/sdi/2013/11/27/plaza-de-bolivar-714091.jpg", "El centro político y cultural de la ciudad."),
        Triple("Zona T", "https://cloudfront-us-east-1.images.arcpublishing.com/infobae/T6XMHHZHLNEYPHTAG7HG6R3OD4.jpeg", "Una de las mejores zonas para la vida nocturna y gastronomía."),
        Triple("Jardín Botánico", "https://images.adsttc.com/media/images/6080/d60e/e6cf/df01/64fc/4ad4/newsletter/dsc4524.jpg?1619056204", "Un espacio natural con gran diversidad de flora."),
        Triple("Usaquén", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTjP9Azz6r6CqVlAI0AZvc7PCnDUWcp5rex_g&s", "Un barrio tradicional con mercados artesanales y restaurantes."),
        Triple("Chorro de Quevedo", "https://images.hive.blog/p/8DAuGnTQCLptZgjHUrRAJGcW4y1D4A5QVJJ7zjzqqKdfVHSS6NapSCCAhET8AGStKpbEh72YGjcyDAVeCetw8EbCUpCJcxmXkUnekNHLCS8X66abRwkQJH7LP7kByG3DUfLC3nESxEniuVyX92oRHwXjnyZKturEEHR427sH54A?format=match&mode=fit", "Lugar emblemático con grafitis y cultura callejera."),
        Triple("Maloka", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRPh_PNIG1HuZSJVa4RADLdFRL-XUAGG2UCOA&s", "Centro interactivo de ciencia y tecnología.")
    )

    val users = listOf("JuanPerez", "AnaGomez", "CarlosRod", "LuisaF", "SantiagoM", "ValeriaP")

    SharedScaffold(selectedTab = 1, onTabSelected = onTabSelected) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(posts) { (location, imageUrl, caption) ->
                val username = users.random() // Nombre de usuario aleatorio

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
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
                                        text = location,
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



