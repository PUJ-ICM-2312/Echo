package com.example.proyecto.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.R
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
import kotlinx.coroutines.launch
import kotlin.random.Random


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
    var selectedGroup by remember { mutableStateOf("Familia") }
    val groups = listOf("Familia", "Amigos", "Trabajo")
    var expanded by remember { mutableStateOf(false) }
    var hotZonesActive by remember { mutableStateOf(false) }
    var hotZoneLocations by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    val apiKey = "AIzaSyDUCTNSx56tPNoSq69obAqGTN4ijTnUf1I"
    val polylinePoints = remember { mutableStateListOf<LatLng>() }


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

    // Estado para controlar la posición de la cámara del mapa
    val cameraPositionState = rememberCameraPositionState {
        // Se puede establecer una posición inicial por defecto si se desea,
        // pero se sobrescribirá cuando se obtenga la ubicación del usuario.
        position = CameraPosition.fromLatLngZoom(LatLng(4.60971, -74.08175), 5f) // Bogotá como fallback inicial lejano
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
    val destinoLatLng = if (latitude != null && longitude != null) LatLng(latitude, longitude) else null

    // Cuando el usuario y el destino están disponibles, trazar la ruta automáticamente
    LaunchedEffect(userLocation, destinoLatLng) {
        if (userLocation != null && destinoLatLng != null) {
            val routePoints = getRouteFromGoogle(userLocation!!, destinoLatLng, apiKey)
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



// Modelo de datos para los marcadores
data class MarkerData(val position: LatLng, val title: String)