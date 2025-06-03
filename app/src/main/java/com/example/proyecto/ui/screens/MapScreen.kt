package com.example.proyecto.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.R
import com.example.proyecto.data.AmigosRepository
import com.example.proyecto.ui.components.SharedScaffold
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.proyecto.utils.LocationHelper
import com.example.proyecto.utils.LocationPermissionHandler
import com.example.proyecto.utils.getRouteFromGoogle
import com.example.proyecto.sensor.LightSensor
import com.example.proyecto.utils.model.Amigo
import com.example.proyecto.utils.model.Ubicacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.proyecto.ui.components.RainEffect
import com.example.proyecto.utils.startTrackingUserLocation
import kotlinx.coroutines.Dispatchers


@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    location: String? = null, // Ubicación puede ser opcional
    latitude: Double? = null,
    longitude: Double? = null,
    onTabSelected: (Int) -> Unit = {}, // Manejador de tab
    navController: NavController // Navegador
) {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val coroutineScope = rememberCoroutineScope()
    var selectedGroup by remember { mutableStateOf("general") }
    var expanded by remember { mutableStateOf(false) }
    var hotZonesActive by remember { mutableStateOf(false) }
    var hotZoneLocations by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val apiKey = "AIzaSyDUCTNSx56tPNoSq69obAqGTN4ijTnUf1I"
    val polylinePoints = remember { mutableStateListOf<LatLng>() }
    var allFriends by remember { mutableStateOf<List<Amigo>>(emptyList()) }
    val database = FirebaseDatabase.getInstance().reference
    var groups by remember { mutableStateOf(listOf<String>()) }

    var selectedFriend by remember { mutableStateOf<Pair<String, LatLng>?>(null) }
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val accelerometer = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }
    val magneticField = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }
    var selectedAmigo by remember { mutableStateOf<Amigo?>(null) }
    var showAmigoOptions by remember { mutableStateOf(false) }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }


    var lastAccelerometerValues by remember { mutableStateOf(FloatArray(3)) }
    var lastMagneticValues by remember { mutableStateOf(FloatArray(3)) }
    var azimuthDegrees by remember { mutableStateOf(0f) }
    val sensorListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        lastAccelerometerValues = event.values.clone()
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        lastMagneticValues = event.values.clone()
                    }
                }
                if (lastAccelerometerValues.isNotEmpty() && lastMagneticValues.isNotEmpty()) {
                    val R = FloatArray(9)
                    val I = FloatArray(9)
                    val success = SensorManager.getRotationMatrix(
                        R, I,
                        lastAccelerometerValues,
                        lastMagneticValues
                    )
                    if (success) {
                        val orientation = FloatArray(3)
                        SensorManager.getOrientation(R, orientation)
                        val azimutRad = orientation[0]
                        azimuthDegrees = Math.toDegrees(azimutRad.toDouble()).toFloat()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUserUid = auth.currentUser?.uid ?: return
    var friendsUids = mutableStateListOf<String>()
    val friendsLocations = remember { mutableStateMapOf<String, LatLng>() }
    var destinoLatLng by remember { mutableStateOf<LatLng?>(null) }
    var showNewGroupDialog by remember { mutableStateOf(false) }
    var newGroupText by remember { mutableStateOf(TextFieldValue("")) }

    var showFriendsMenu by remember { mutableStateOf(false) }



// Estado para la Polyline (ruta del usuario)
    var path by remember { mutableStateOf(listOf<LatLng>()) }


    // Estado para la ubicación del usuario
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember { mutableStateOf(false) }

    // Generar nuevas zonas calientes
    LaunchedEffect(hotZonesActive) {
        if (hotZonesActive && userLocation != null) {
            val randomLocations = List(20) {
                generateRandomLatLng(userLocation!!.latitude, userLocation!!.longitude, 10000.0)
            }
            hotZoneLocations = randomLocations
        }
    }

    var isRaining by remember { mutableStateOf(false) }
    val pressureSensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
    }
    val pressureListener = remember {
        object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val pressure = event.values[0]
                // Consideramos que presión baja es menor a 1000 hPa (puedes ajustar)
                isRaining = pressure < 1000f
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    // Agrega este DisposableEffect para registrar el sensor
    DisposableEffect(sensorManager) {
        sensorManager.registerListener(
            pressureListener,
            pressureSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        onDispose {
            sensorManager.unregisterListener(pressureListener)
        }
    }

    // Dentro de tu composable o ViewModel (dependiendo de tu estructura)
    LaunchedEffect(currentUserUid,selectedGroup) {
        friendsLocations.clear()

        AmigosRepository().obtenerAmigos(currentUserUid) { lista ->
            allFriends = lista
            // Filtrar amigos según el grupo seleccionado
            val amigosDelGrupo = lista.filter { it.grupo == selectedGroup }

            // Solo dejamos los UID de los amigos que pertenecen a ese grupo
            friendsUids = amigosDelGrupo.map { it.uid }.toMutableStateList()

            Log.d("MapScreenDebug", "Total amigos cargados: ${allFriends.size}")
            Log.d("MapScreenDebug", "Amigos en grupo '$selectedGroup': ${amigosDelGrupo.size}")
            // Ahora que allFriends está actualizado, arrancamos el listener de Firestore
            val amigosRef = database
                .child("usuarios")

            // … dentro de LaunchedEffect(currentUserUid) …
            friendsUids.forEach { uid ->
                val amigoRef = amigosRef.child(uid)
                amigoRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // 1) Verificamos si "latitud" existe como objeto Ubicacion (campo "latitude")
                        val latNode = snapshot.child("latitud")
                        var lat: Double? = null
                        var lng: Double? = null

                        if (latNode.hasChild("latitude") && latNode.hasChild("longitude")) {
                            // Caso A: "latitud" es un objeto con dos campos {"latitude":…, "longitude":…}
                            val ubicacionObj = latNode.getValue(Ubicacion::class.java)
                            lat = ubicacionObj?.latitude
                            lng = ubicacionObj?.longitude
                        } else {
                            // Caso B: "latitud" viene como Double primitivo (y tal vez "longitud" está aparte)
                            val primitiveLat = latNode.getValue(Double::class.java)
                            val primitiveLng = snapshot.child("longitud").getValue(Double::class.java)
                            lat = primitiveLat
                            lng = primitiveLng
                        }

                        val conectado = snapshot.child("conectado").getValue(Boolean::class.java) ?: false

                        Log.d("MapScreenDebug", "Datos amigo: uid=$uid, lat=$lat, lng=$lng, conectado=$conectado")

                        if (lat != null && lng != null ) {
                            val username = snapshot.child("username").getValue(String::class.java) ?: uid
                            friendsLocations[username] = LatLng(lat, lng)
                            Log.d("MapScreenDebug", "Ubicación actualizada para amigo $username")
                        } else {
                            friendsLocations.remove(uid)
                            Log.d("MapScreenDebug", "Amigo $uid desconectado o sin ubicación, removido")
                        }
                    }



                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MapScreenDebug", "Error en listener de $uid: ${error.message}")
                    }
                })
            }


        }
        AmigosRepository().obtenerGruposUsuario(currentUserUid) { listaLeida ->
            val temp = listaLeida.toMutableList()
            if (!temp.contains("general")) {
                temp.add(0, "general")
            }
            groups = temp
            if (selectedGroup.isBlank()) {
                selectedGroup = "general"
            }
        }
        AmigosRepository().obtenerAmigos(currentUserUid) { lista ->
            allFriends = lista
        }
    }


    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(4.60971, -74.08175), 5f)
    }
    LaunchedEffect(azimuthDegrees) {
        val currentTarget = cameraPositionState.position.target
        val currentZoom = cameraPositionState.position.zoom
        val currentTilt = cameraPositionState.position.tilt
        val nuevaPosicion = CameraPosition(
            currentTarget,
            currentZoom,
            currentTilt,
            azimuthDegrees
        )
        cameraPositionState.animate(
            update = CameraUpdateFactory.newCameraPosition(nuevaPosicion),
            durationMs = 500
        )
    }

// Estado para saber si ya se hizo el movimiento inicial de cámara
    var isInitialCameraMoveDone by remember { mutableStateOf(false) }



    LocationPermissionHandler {
        hasPermission = true
    }


    DisposableEffect(hasPermission) {
        if (hasPermission) {
            val locationCallback = locationHelper.registerLocationUpdates { latLng ->
                path = path + latLng
                userLocation = latLng
            }

            onDispose {
                locationHelper.fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        } else {
            onDispose {}
        }
    }

    var stopTracking by remember { mutableStateOf<(() -> Unit)?>(null) }

    DisposableEffect(hasPermission) {
        if (hasPermission) {
            stopTracking = startTrackingUserLocation(
                context = context,
                userId = currentUserUid,
                coroutineScope = coroutineScope,
                onLocationUpdated = null
            )
        }
        onDispose {
            stopTracking?.invoke()
        }
    }

    DisposableEffect(sensorManager) {
        sensorManager.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
        sensorManager.registerListener(
            sensorListener,
            magneticField,
            SensorManager.SENSOR_DELAY_UI
        )
        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    // Efecto para mover la cámara a la ubicación inicial del usuario
    LaunchedEffect(userLocation) {
        // Se ejecutará cada vez que userLocation cambie de null a un valor
        if (userLocation != null && !isInitialCameraMoveDone) {
            // Anima la cámara a la nueva posición (ubicación actual) con zoom 15
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f),
                durationMs = 1000 // Duración de la animación en milisegundos (opcional)
            )
            isInitialCameraMoveDone = true // Marca que el movimiento inicial ya se hizo
        }
    }


    if (!hasPermission) {
        Text("Se requiere permiso de ubicación para usar el mapa.",
            modifier = Modifier.padding(16.dp).fillMaxSize().statusBarsPadding(),
            textAlign = TextAlign.Center
        )
        return
    }

    // Estado para la búsqueda de direcciones
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var searchLocation by remember { mutableStateOf<LatLng?>(null) }

    // Estado para los marcadores
    var markers by remember { mutableStateOf(listOf<MarkerData>()) }


    // Estado para el estilo del mapa:
    var isDarkMap by remember { mutableStateOf(false) }

    // Usar el sensor:
    LightSensor { isLowLight ->
        isDarkMap = isLowLight
    }

    // Configurar el estilo del mapa:
    val mapProperties by remember(isDarkMap) {
        mutableStateOf(
            MapProperties(
                mapStyleOptions = if (isDarkMap) {
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.dark_map_style)
                } else {
                    null // Usa el estilo por defecto (claro)
                },
                isMyLocationEnabled = true
            )
        )
    }

    // Coordenadas recibidas
    destinoLatLng = if (latitude != null && longitude != null) LatLng(latitude, longitude) else null

    // Cuando el usuario y el destino están disponibles, trazar la ruta automáticamente
    LaunchedEffect(userLocation, destinoLatLng?.latitude, destinoLatLng?.longitude) {

        if (userLocation != null && destinoLatLng != null) {
            Log.d("RUTA", "userLocation: $userLocation, destinoLatLng: $destinoLatLng")
            val routePoints = getRouteFromGoogle(userLocation!!, destinoLatLng!!, apiKey)
            Log.d("RUTA", "Puntos de ruta obtenidos: ${routePoints.size}")
            polylinePoints.clear()
            polylinePoints.addAll(routePoints)

        }
    }


    // Configuración de la UI del mapa
    val mapUiSettings = MapUiSettings(
        myLocationButtonEnabled = true,
        zoomControlsEnabled = false
    )

    SharedScaffold(selectedTab = 0, onTabSelected = onTabSelected, navController = navController) {

        // Estructura de la interfaz de usuario
        Box {
            // Mapa de Google
            GoogleMap(
                contentPadding = PaddingValues(
                    top = 150.dp
                ),
                properties = mapProperties,
                uiSettings = mapUiSettings,
                cameraPositionState = cameraPositionState, // Usar el estado de la cámara
                onMapLongClick = { latLng ->
                    coroutineScope.launch {
                        val address =
                            locationHelper.getAddressFromLatLng(
                                latLng.latitude,
                                latLng.longitude
                            )
                        markers = listOf(MarkerData(latLng, address ?: "Ubicación desconocida"))
                    }
                }
            ) {
                // Muestra la ubicación actual con unicono personalizado
                userLocation?.let { currentLocation ->
                    // Se crea el BitmapDescriptor a partir del recurso drawable (png)
                    val userIcon: BitmapDescriptor =
                        remember(context) { // Recordar para eficiencia
                            BitmapDescriptorFactory.fromResource(R.drawable.user)
                        }

                    Marker(
                        state = MarkerState(position = currentLocation),
                        title = "Tu ubicación",
                        // Se usa el icono personalizado
                        icon = userIcon
                    )
                }

                userLocation?.let { origin ->
                    markers.forEach { marker ->
                        Marker(
                            state = MarkerState(position = marker.position),
                            title = marker.title
                        )
                    }

                    // Dibujar la ruta
                    if (polylinePoints.isNotEmpty()) {
                        Polyline(
                            points = polylinePoints,
                            color = Color.Blue,
                            width = 8f
                        )
                    }
                }


                friendsLocations.forEach { (uid, location) ->
                    Marker(
                        state = MarkerState(position = location),
                        title = "Amigo: $uid", // Puedes mostrar su nombre si lo tienes
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),

                        onClick = {
                            selectedFriend = uid to location // uid es el nombre del amigo en tu mapa
                            true // importante retornar true
                        }

                    )
                }

                // Muestra los marcadores agregados
                markers.forEach { marker ->
                    Marker(
                        state = MarkerState(position = marker.position),
                        title = marker.title
                    )
                }

                // Mostrar marcador en el destino si hay ruta
                if (polylinePoints.isNotEmpty()) {
                    Marker(
                        state = MarkerState(position = polylinePoints.last()),
                        title = "Destino final",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }


                hotZoneLocations.forEach { hotZone ->
                    Marker(
                        state = MarkerState(position = hotZone),
                        title = "Zona caliente",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                    )
                }


                // Dibuja la ruta del usuario
                if (path.isNotEmpty()) {
                    Polyline(
                        points = path,
                        color = androidx.compose.ui.graphics.Color.Blue,
                        width = 10f
                    )
                }
            }


            RainEffect(isRaining)


            selectedFriend?.let { (nombre, ubicacion) ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .shadow(8.dp)
                        .padding(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Amigo: $nombre", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = {
                            trazarRuta(ubicacion, userLocation ?: LatLng(0.0, 0.0), apiKey, coroutineScope, polylinePoints)
                            selectedFriend = null
                        }) {
                            Text("Ir a ubicación")
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(onClick = {
                            selectedFriend = null // cerrar panel
                        }) {
                            Text("Cerrar")
                        }
                    }
                }
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {


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
            FloatingActionButton(
                onClick = { showFriendsMenu = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Face, contentDescription = "Ver amigos")
            }

            val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            val scope = rememberCoroutineScope()

            if (showFriendsMenu) {
                FriendsBottomMenu(
                    bottomSheetState = bottomSheetState,
                    scope = scope,
                    groups = groups,
                    selectedGroup = selectedGroup,
                    onGroupSelected = { group ->
                        selectedGroup = group
                        scope.launch { bottomSheetState.hide() } // Oculta el sheet
                        showFriendsMenu = false
                    },
                    friends = allFriends.filter { it.grupo == selectedGroup },
                    onFriendClick = { amigo ->
                        selectedAmigo = amigo
                        scope.launch { bottomSheetState.hide() }
                        showFriendsMenu = false
                        showAmigoOptions = true
                    },
                    onDismiss = {
                        scope.launch { bottomSheetState.hide() }
                        showFriendsMenu = false
                    }
                )
            }


            if (showAmigoOptions && selectedAmigo != null) {
                AmigoOptionsSheet(
                    amigo = selectedAmigo!!,
                    onDismiss = { showAmigoOptions = false },
                    onCenterMap = {
                        // Centrar la cámara en la ubicación del amigo
                        obtainUsername(selectedAmigo!!.uid) { username ->
                            selectedLocation = friendsLocations[username]
                            Log.d("OpcionesAmigo", "Centrando mapa en ${username} en $selectedLocation")
                        }
                        selectedLocation?.let {
                            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(it, 16f))
                        }
                    },
                    onViewRoute = {
                        obtainUsername(selectedAmigo!!.uid) { username ->
                            val ubicacion = friendsLocations[username]
                            selectedLocation = ubicacion
                            Log.d("OpcionesAmigo", "Centrando mapa en ${username} en $selectedLocation")

                            if (ubicacion != null && userLocation != null) {
                                trazarRuta(ubicacion, userLocation!!, apiKey, coroutineScope, polylinePoints)
                            } else {
                                Log.e("OpcionesAmigo", "No se pudo trazar ruta: ubicación nula")
                            }

                            Log.d("OpcionesAmigo", "Ver ruta de ${selectedAmigo!!.uid}")
                        }
                    }
                )
            }


        }

    }
}

fun trazarRuta(destino: LatLng, userLocation: LatLng, apiKey: String, coroutineScope: CoroutineScope, polylinePoints: MutableList<LatLng>) {
    Log.d("RUTA", "Iniciando trazado ruta a $destino desde $userLocation")
    if (userLocation == null) {
        Log.d("RUTA", "userLocation es null, no puedo trazar ruta")
        return
    }
    coroutineScope.launch {
        val routePoints = getRouteFromGoogle(userLocation!!, destino, apiKey)
        if (routePoints.isNotEmpty()) {
            polylinePoints.clear()
            polylinePoints.addAll(routePoints)
            Log.d("RUTA", "Ruta trazada con ${routePoints.size} puntos")
        } else {
            Log.d("RUTA", "No se encontraron puntos para la ruta")
        }
    }

}

fun obtainUsername(uid: String, onResult: (String?) -> Unit) {
    val dbRef = FirebaseDatabase.getInstance().reference
        .child("usuarios")
        .child(uid)
        .child("username")

    dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val username = snapshot.getValue(String::class.java)
            onResult(username)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Error al obtener username: ${error.message}")
            onResult(null)
        }
    })
}




fun generateRandomLatLng(centerLat: Double, centerLng: Double, radiusInMeters: Double): LatLng {
    // Genera un ángulo aleatorio en radianes
    val randomAngle = Random.nextDouble(0.0, 2 * Math.PI)

    // Distancia aleatoria dentro del radio en metros
    val randomDistance = Random.nextDouble(0.0, radiusInMeters)

    // Cálculo de la nueva latitud y longitud a partir del centro y la distancia aleatoria
    val earthRadius = 6371000.0 // Radio de la Tierra en metros
    val latChange = (randomDistance / earthRadius) * (180 / Math.PI)
    val lngChange = (randomDistance / earthRadius) * (180 / Math.PI) / Math.cos(Math.toRadians(centerLat))

    // Nueva latitud y longitud
    val randomLat = centerLat + latChange * Math.sin(randomAngle)
    val randomLng = centerLng + lngChange * Math.cos(randomAngle)

    return LatLng(randomLat, randomLng)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsBottomMenu(
    bottomSheetState: SheetState,
    scope: CoroutineScope,
    selectedGroup: String,
    groups: List<String>,
    onGroupSelected: (String) -> Unit,
    friends: List<Amigo>,
    onFriendClick: (Amigo) -> Unit,
    onDismiss: () -> Unit
) {


    var username by remember { mutableStateOf<String?>(null) }



    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                bottomSheetState.hide()
                onDismiss()
            }
        },
        sheetState = bottomSheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Dropdown de grupos
            var expanded by remember { mutableStateOf(false) }

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
                    modifier = Modifier.menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    groups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group) },
                            onClick = {
                                onGroupSelected(group)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Amigos del grupo '$selectedGroup'", style = MaterialTheme.typography.titleMedium)

            LazyColumn {
                items(friends) { amigo ->
                    LaunchedEffect(amigo.uid) {
                        obtainUsername(amigo.uid) { name ->
                            username = name
                        }
                    }
                    ListItem(
                        headlineContent = { username?.let { Text(it) } },
                        supportingContent = {
                            Text("Conectado")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onFriendClick(amigo) }
                    )
                }
            }
        }
    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmigoOptionsSheet(
    amigo: Amigo,
    onDismiss: () -> Unit,
    onCenterMap: () -> Unit,
    onViewRoute: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Opciones para ${amigo.uid}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                onCenterMap()
                onDismiss()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Centrar en el mapa")
            }

            Button(onClick = {
                onViewRoute()
                onDismiss()
            }, modifier = Modifier.fillMaxWidth()) {
                Text("Ver ruta")
            }

        }
    }
}





// Modelo de datos para los marcadores
data class MarkerData(val position: LatLng, val title: String)