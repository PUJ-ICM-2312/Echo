package com.example.proyecto.ui.screens.chat

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.proyecto.data.AmigosRepository
import com.example.proyecto.navigation.Screen
import com.example.proyecto.ui.components.obtenerUsernamePorUid
import com.example.proyecto.utils.model.Amigo
import com.example.proyecto.utils.model.Ubicacion
import com.example.proyecto.utils.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendListScreen(navController: NavController) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUid = currentUser?.uid ?: return
    val database = Firebase.database.reference
    val coroutineScope = rememberCoroutineScope()
    var groups by remember { mutableStateOf(listOf<String>()) }
    var selectedGroup by remember { mutableStateOf("general") }
    var expandedGroupDropdown by remember { mutableStateOf(false) }

    var allFriends by remember { mutableStateOf<List<Amigo>>(emptyList()) }
    LaunchedEffect(currentUid) {
        AmigosRepository().obtenerGruposUsuario(currentUid) { listaLeida ->
            val temp = listaLeida.toMutableList()
            if (!temp.contains("general")) {
                temp.add(0, "general")
            }
            groups = temp
            if (selectedGroup.isBlank()) {
                selectedGroup = "general"
            }
        }
        AmigosRepository().obtenerAmigos(currentUid) { lista ->
            allFriends = lista
        }
    }





    // Estado para crear un nuevo grupo (texto + mostrar diálogo)
    var newGroupText by remember { mutableStateOf(TextFieldValue("")) }
    var showNewGroupDialog by remember { mutableStateOf(false) }

    // ───── Estados para la BÚSQUEDA por @username ─────
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Usuario>>(emptyList()) }
    val isSearching = searchQuery.isNotBlank()

    // ───── Cuando cambie searchQuery, agregamos/removemos el listener a /usuarios ─────
    DisposableEffect(searchQuery) {
        if (isSearching) {
            val q: Query = database.child("usuarios")
                .orderByChild("username")
                .startAt(searchQuery)
                .endAt("$searchQuery\uf8ff")

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val resultados = mutableListOf<Usuario>()
                    val usuariosRef = FirebaseDatabase.getInstance().reference.child("usuarios")

                    snapshot.children.forEach { childSnap ->
                        val uidAmigo = childSnap.key ?: return@forEach

                        usuariosRef.child(uidAmigo)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnap: DataSnapshot) {
                                    val value = userSnap.value
                                    if (value is Map<*, *>) {
                                        val nombre = value["nombre"] as? String ?: ""
                                        val cedula = value["cedula"] as? String ?: ""
                                        val username = value["username"] as? String ?: ""
                                        val correoElectronico = value["correoElectronico"] as? String ?: ""
                                        val fotoDePerfilUrl = value["fotoDePerfilUrl"] as? String ?: ""
                                        val telefono = value["telefono"] as? String ?: ""
                                        val conectado = value["conectado"] as? Boolean ?: false

                                        val latitudMap = value["latitud"] as? Map<*, *>
                                        val latitud = if (latitudMap != null) {
                                            Ubicacion (latitudMap["latitude"] as? Double ?: 0.0)
                                            Ubicacion (latitudMap["longitude"] as? Double ?: 0.0)
                                        } else null

                                        val amigosMap = value["amigos"] as? Map<*, *>
                                        val amigos = amigosMap?.mapNotNull { (k, v) ->
                                            val amigoMap = v as? Map<*, *> ?: return@mapNotNull null
                                            val amigo = Amigo(
                                                uid = amigoMap["uid"] as? String ?: "",
                                                grupo = amigoMap["grupo"] as? String ?: ""
                                            )
                                            (k as? String)?.let { it to amigo }
                                        }?.toMap() ?: emptyMap()

                                        val sentRequestsMap = value["sentRequests"] as? Map<*, *>
                                        val sentRequests = sentRequestsMap?.mapNotNull { (k, v) ->
                                            val reqMap = v as? Map<*, *> ?: return@mapNotNull null
                                            val usuario = Usuario(
                                                uid = reqMap["uid"] as? String ?: "",
                                                nombre = reqMap["nombre"] as? String ?: "",
                                                username = reqMap["username"] as? String ?: ""
                                            )
                                            (k as? String)?.let { it to usuario }
                                        }?.toMap() ?: emptyMap()

                                        val receivedRequestsMap = value["receivedRequests"] as? Map<*, *>
                                        val receivedRequests = receivedRequestsMap?.mapNotNull { (k, v) ->
                                            val reqMap = v as? Map<*, *> ?: return@mapNotNull null
                                            val usuario = Usuario(
                                                uid = reqMap["uid"] as? String ?: "",
                                                nombre = reqMap["nombre"] as? String ?: "",
                                                username = reqMap["username"] as? String ?: ""
                                            )
                                            (k as? String)?.let { it to usuario }
                                        }?.toMap() ?: emptyMap()

                                        val usuario = Usuario(
                                            uid = uidAmigo,
                                            nombre = nombre,
                                            cedula = cedula,
                                            username = username,
                                            correoElectronico = correoElectronico,
                                            fotoDePerfilUrl = fotoDePerfilUrl,
                                            telefono = telefono,
                                            conectado = conectado,
                                            latitud = latitud as Ubicacion?,
                                            amigos = amigos,
                                            sentRequests = sentRequests,
                                            receivedRequests = receivedRequests
                                        )

                                        resultados.add(usuario)
                                        searchResults = resultados.toList()
                                    } else {
                                        Log.w("FriendListScreen", "Nodo $uidAmigo no es un objeto Usuario")
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("FriendListScreen", "Error al obtener usuario $uidAmigo: ${error.message}")
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FriendListScreen", "Error en búsqueda: ${error.message}")
                }
            }

            q.addValueEventListener(listener)

            onDispose {
                q.removeEventListener(listener)
            }
        }
        onDispose { }
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Amigos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.FriendRequest.route)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Notifications, // o cualquier otro ícono que prefieras
                            contentDescription = "Solicitudes de amistad"
                        )
                    }
                }
            )

        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedGroupDropdown,
                    onExpandedChange = { expandedGroupDropdown = !expandedGroupDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedGroup,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Grupo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedGroupDropdown) },
                        modifier = Modifier
                            .weight(1f)
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGroupDropdown,
                        onDismissRequest = { expandedGroupDropdown = false }
                    ) {
                        groups.forEach { groupName ->
                            DropdownMenuItem(
                                text = { Text(groupName) },
                                onClick = {
                                    selectedGroup = groupName
                                    expandedGroupDropdown = false
                                }
                            )
                        }
                        // Opción para crear uno nuevo
                        DropdownMenuItem(
                            text = { Text("➕ Crear grupo…") },
                            onClick = {
                                expandedGroupDropdown = false
                                showNewGroupDialog = true
                            }
                        )
                    }
                }
            }

            if (showNewGroupDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showNewGroupDialog = false
                        newGroupText = TextFieldValue("")
                    },
                    title = { Text("Nuevo grupo") },
                    text = {
                        OutlinedTextField(
                            value = newGroupText,
                            onValueChange = { newGroupText = it },
                            label = { Text("Nombre de grupo") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            val trimmed = newGroupText.text.trim()
                            if (trimmed.isNotEmpty() && !groups.contains(trimmed)) {
                                groups = groups + trimmed
                                selectedGroup = trimmed
                                coroutineScope.launch(Dispatchers.IO) {
                                    AmigosRepository().agregarGrupoUsuario(currentUid, trimmed)
                                }
                            }
                            newGroupText = TextFieldValue("")
                            showNewGroupDialog = false
                        }) {
                            Text("Crear")
                        }

                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showNewGroupDialog = false
                            newGroupText = TextFieldValue("")
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            Divider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { input ->
                        searchQuery = input.trimStart('@')
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    placeholder = { Text("Buscar por @username") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Buscar") }
                )
                IconButton(onClick = { /* Botón “Pendientes” */ }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Pendientes")
                }
            }

            Divider()

            // ───── Filtrar `allFriends` por `selectedGroup` ─────
            val friendsFiltered = remember(allFriends, selectedGroup) {
                if (selectedGroup == "general") {
                    allFriends
                } else {
                    allFriends.filter { it.grupo == selectedGroup }
                }
            }

            // ───── Mostrar resultados de búsqueda o la lista filtrada ─────
            if (isSearching) {
                if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron usuarios con “@$searchQuery”",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = searchResults,
                            key = { usuario -> usuario.uid }
                        ) { usuario ->
                            SearchResultItem(usuario = usuario) {
                                navController.navigate(Screen.FriendProfile.createRoute(usuario.uid))
                            }
                            Divider()
                        }
                    }
                }

            } else {
                if (friendsFiltered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (selectedGroup == "general")
                                "Aún no tienes amigos agregados.\nBusca por @username para añadir."
                            else
                                "No tienes amigos en \"$selectedGroup\".\nAgrégalos al grupo desde la lista.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = friendsFiltered,
                            key = { amigo -> amigo.uid }
                        ) { amigo ->
                            FriendListItem(
                                amigo = amigo,
                                onClick = {
                                    navController.navigate(Screen.DirectChat.createRoute(amigo.uid))
                                },
                                onChangeGroup = { nuevoGrupo ->
                                    amigo.grupo = nuevoGrupo
                                    coroutineScope.launch(Dispatchers.IO) {
                                        AmigosRepository().actualizarGrupoDeAmigo(
                                            usuarioId = currentUid,
                                            amigoId = amigo.uid,
                                            nuevoGrupo = nuevoGrupo
                                        )
                                    }
                                },
                                onDelete = {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        AmigosRepository().eliminarAmigo(currentUid, amigo.uid)
                                        AmigosRepository().obtenerAmigos(currentUid) { nuevaLista ->
                                            coroutineScope.launch(Dispatchers.Main) {
                                                allFriends = nuevaLista
                                            }
                                        }
                                    }
                                },
                                availableGroups = groups
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun SearchResultItem(usuario: Usuario, onClick: () -> Unit) {
    ListItem(
        leadingContent = {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "Usuario encontrado",
                modifier = Modifier.size(32.dp)
            )
        },
        headlineContent = { Text(text = "@${usuario.username}") },
        supportingContent = {
            Text(text = usuario.nombre.ifBlank { usuario.correoElectronico })
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}
@Composable
fun FriendListItem(
    amigo: Amigo,
    onClick: () -> Unit,
    onChangeGroup: (String) -> Unit,
    availableGroups: List<String>,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    ListItem(
        leadingContent = {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "Amigo",
                modifier = Modifier.size(32.dp)
            )
        },
        headlineContent = {
            var username by remember { mutableStateOf<String?>(null) }
            LaunchedEffect(amigo.uid) {
                obtenerUsernamePorUid(amigo.uid) { result ->
                    username = result
                }
            }
            Text(text = username ?: amigo.email.ifBlank { "@${amigo.uid}" })
        },
        supportingContent = {
            Text(
                text = "Grupo: ${amigo.grupo}",
                style = MaterialTheme.typography.bodySmall
            )
        },
        trailingContent = {
            Row {
                // 1) Botón para mostrar el menú de cambiar grupo
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Cambiar grupo"
                    )
                }
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    availableGroups.forEach { groupName ->
                        DropdownMenuItem(
                            text = { Text(groupName) },
                            onClick = {
                                expandedMenu = false
                                onChangeGroup(groupName)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 2) Botón para ELIMINAR este amigo
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar amigo",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
}