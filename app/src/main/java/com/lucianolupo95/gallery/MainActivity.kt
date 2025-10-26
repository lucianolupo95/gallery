package com.lucianolupo95.gallery

import android.app.RecoverableSecurityException
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable // ⬅️ IMPORT CLAVE
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.lucianolupo95.gallery.ui.ImageDetailScreen
import com.lucianolupo95.gallery.ui.MainScreen
import com.lucianolupo95.gallery.ui.theme.GalleryTheme
import com.lucianolupo95.gallery.util.PermissionManager
import com.lucianolupo95.gallery.viewmodel.GalleryViewModel
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager
    private var sdCardUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)
        permissionManager.requestPermission()

        // Recuperar permiso persistente SAF si existiera
        contentResolver.persistedUriPermissions.firstOrNull()?.let {
            sdCardUri = it.uri
        }

        setContent {
            var isSelectionMode by remember { mutableStateOf(false) }

            GalleryTheme {
                val viewModel: GalleryViewModel by viewModels()
                val images by viewModel.images.collectAsState()
                val folders by viewModel.folders.collectAsState()

                var selectedIndex by remember { mutableStateOf<Int?>(null) }
                var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }

                var currentFolder by remember { mutableStateOf<String?>(null) }
                var showFolders by remember { mutableStateOf(true) }
                var showMoveDialog by remember { mutableStateOf(false) }

                val scope = rememberCoroutineScope()

                // Cargar datos tras permisos
                LaunchedEffect(permissionManager.hasPermission.value) {
                    if (permissionManager.hasPermission.value) {
                        viewModel.sdCardUri = sdCardUri
                        viewModel.loadFolders()
                        viewModel.loadImages()
                    }
                }

                // SAF: elegir carpeta SD
                val sdAccessLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocumentTree()
                ) { uri ->
                    if (uri != null) {
                        sdCardUri = uri
                        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        contentResolver.takePersistableUriPermission(uri, flags)
                        viewModel.sdCardUri = uri
                        viewModel.loadFolders()
                        Toast.makeText(this, "📂 Acceso concedido a la tarjeta SD", Toast.LENGTH_SHORT).show()
                    }
                }

                // Launcher para borrado (Android 11+)
                val deleteLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult()
                ) { _ ->
                    scope.launch(Dispatchers.Main) {
                        delay(300)
                        if (currentFolder == null) viewModel.loadImages()
                        else viewModel.loadImagesFromFolder(currentFolder!!)
                        Toast.makeText(this@MainActivity, "🗑️ Imagen eliminada (recargando galería)", Toast.LENGTH_SHORT).show()
                        selectedIndex = null
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        // Detalle
                        selectedIndex != null -> {
                            ImageDetailScreen(
                                imageUris = images,
                                startIndex = selectedIndex!!,
                                onBackClick = { selectedIndex = null },
                                onRequestDelete = { uri ->
                                    scope.launch {
                                        try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                val pending = MediaStore.createDeleteRequest(contentResolver, listOf(uri))
                                                deleteLauncher.launch(
                                                    IntentSenderRequest.Builder(pending.intentSender).build()
                                                )
                                            } else {
                                                withContext(Dispatchers.IO) {
                                                    val deleted = contentResolver.delete(uri, null, null)
                                                    withContext(Dispatchers.Main) {
                                                        if (deleted > 0) {
                                                            Toast.makeText(this@MainActivity, "🗑️ Imagen eliminada", Toast.LENGTH_SHORT).show()
                                                        } else {
                                                            Toast.makeText(this@MainActivity, "⚠️ No se pudo eliminar", Toast.LENGTH_SHORT).show()
                                                        }
                                                        selectedIndex = null
                                                        delay(300)
                                                        if (currentFolder == null) viewModel.loadImages()
                                                        else viewModel.loadImagesFromFolder(currentFolder!!)
                                                    }
                                                }
                                            }
                                        } catch (e: RecoverableSecurityException) {
                                            val sender: IntentSender = e.userAction.actionIntent.intentSender
                                            deleteLauncher.launch(IntentSenderRequest.Builder(sender).build())
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Toast.makeText(this@MainActivity, "⚠️ Error inesperado", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            )
                        }

                        // Principal
                        else -> {
                            MainScreen(
                                hasPermission = permissionManager.hasPermission.value,
                                images = images,
                                selectedImages = selectedImages,
                                onImageClick = { index ->
                                    if (isSelectionMode) {
                                        val image = images[index]
                                        selectedImages = if (selectedImages.contains(image)) {
                                            selectedImages - image
                                        } else {
                                            selectedImages + image
                                        }
                                    } else {
                                        selectedIndex = index
                                    }
                                },
                                onRequestPermissionClick = { permissionManager.requestPermission() },
                                folders = folders,
                                onFolderClick = { name ->
                                    currentFolder = name
                                    showFolders = false
                                    viewModel.loadImagesFromFolder(name)
                                    selectedImages = emptyList()
                                    isSelectionMode = false
                                },
                                onShowAllClick = {
                                    currentFolder = null
                                    showFolders = false
                                    viewModel.loadImages()
                                    selectedImages = emptyList()
                                    isSelectionMode = false
                                },
                                showFolders = showFolders,
                                onBackClick = {
                                    selectedImages = emptyList()
                                    isSelectionMode = false
                                    currentFolder = null
                                    showFolders = true
                                    viewModel.loadFolders()
                                },
                                onCreateFolder = { name ->
                                    val ok = viewModel.createFolder(name)
                                    Toast.makeText(this, if (ok) "📁 Carpeta creada" else "⚠️ No se pudo crear", Toast.LENGTH_SHORT).show()
                                },
                                onDeleteFolder = { name ->
                                    val ok = viewModel.deleteFolder(name)
                                    Toast.makeText(this, if (ok) "🗑️ Carpeta eliminada" else "⚠️ No se pudo eliminar", Toast.LENGTH_SHORT).show()
                                },
                                onRenameFolder = { old, new ->
                                    val ok = viewModel.renameFolder(old, new)
                                    Toast.makeText(this, if (ok) "✏️ Carpeta renombrada" else "⚠️ No se pudo renombrar", Toast.LENGTH_SHORT).show()
                                },
                                currentFolder = currentFolder,
                                onSelectionChange = { selectedImages = it },
                                onMoveSelectedClick = { if (selectedImages.isNotEmpty()) showMoveDialog = true },
                                onCancelSelection = {
                                    selectedImages = emptyList()
                                    isSelectionMode = false
                                },
                                isSelectionMode = isSelectionMode,
                                onToggleSelectionMode = { isSelectionMode = !isSelectionMode },
                                onSelectionModeChange = { mode -> isSelectionMode = mode },
                                isSdStorage = sdCardUri != null
                            )

                            // --- Diálogo "Mover a carpeta" ---
                            if (showMoveDialog && selectedImages.isNotEmpty()) {
                                var showNewFolderDialog by remember { mutableStateOf(false) }
                                var newFolderName by remember { mutableStateOf("") }

                                AlertDialog(
                                    onDismissRequest = { showMoveDialog = false },
                                    title = { Text("Mover a carpeta") },
                                    text = {
                                        Column {
                                            Text("Seleccioná la carpeta de destino:")
                                            Spacer(modifier = Modifier.height(8.dp))

                                            LazyColumn(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 360.dp)
                                            ) {
                                                items(folders) { folder ->
                                                    ListItem(
                                                        headlineContent = { Text(folder.name) },
                                                        supportingContent = {
                                                            Text("${folder.imageCount} imágenes")
                                                        },
                                                        leadingContent = {
                                                            if (folder.thumbnailUri != null) {
                                                                Image(
                                                                    painter = rememberAsyncImagePainter(folder.thumbnailUri),
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(40.dp)
                                                                )
                                                            } else {
                                                                Icon(
                                                                    imageVector = Icons.Default.Folder,
                                                                    contentDescription = null
                                                                )
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 4.dp)
                                                            .clickable {
                                                                Toast.makeText(
                                                                    this@MainActivity,
                                                                    "Moviendo ${selectedImages.size} a '${folder.name}'…",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()

                                                                scope.launch {
                                                                    val ok = viewModel.moveImagesToFolder(
                                                                        selectedImages,
                                                                        folder.name
                                                                    )

                                                                    Toast.makeText(
                                                                        this@MainActivity,
                                                                        if (ok) "✅ Movimiento completado" else "⚠️ Error al mover",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()

                                                                    // cerrar y refrescar
                                                                    showMoveDialog = false
                                                                    selectedImages = emptyList()
                                                                    isSelectionMode = false

                                                                    if (currentFolder == null)
                                                                        viewModel.loadImages()
                                                                    else
                                                                        viewModel.loadImagesFromFolder(currentFolder!!)

                                                                    viewModel.loadFolders()
                                                                }
                                                            }
                                                    )
                                                    Divider()
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            TextButton(onClick = { showNewFolderDialog = true }) {
                                                Text("➕ Crear nueva carpeta y mover aquí")
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))
                                            TextButton(onClick = { showMoveDialog = false }) {
                                                Text("Cancelar", color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    },
                                    confirmButton = {}
                                )

                                // Crear carpeta y mover en un paso
                                if (showNewFolderDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showNewFolderDialog = false },
                                        title = { Text("Nueva carpeta") },
                                        text = {
                                            OutlinedTextField(
                                                value = newFolderName,
                                                onValueChange = { newFolderName = it },
                                                label = { Text("Nombre de la carpeta") },
                                                singleLine = true
                                            )
                                        },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                val name = newFolderName.trim()
                                                if (name.isNotEmpty()) {
                                                    scope.launch {
                                                        val created = viewModel.createFolder(name)
                                                        val moved = viewModel.moveImagesToFolder(selectedImages, name)

                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            when {
                                                                created && moved -> "📂 '$name' creada y ${selectedImages.size} movidas"
                                                                moved -> "📦 Imágenes movidas a '$name'"
                                                                else -> "⚠️ Error al mover"
                                                            },
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        showNewFolderDialog = false
                                                        showMoveDialog = false
                                                        selectedImages = emptyList()
                                                        isSelectionMode = false

                                                        if (currentFolder == null)
                                                            viewModel.loadImages()
                                                        else
                                                            viewModel.loadImagesFromFolder(currentFolder!!)

                                                        viewModel.loadFolders()
                                                    }
                                                }
                                            }) { Text("Crear y mover") }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showNewFolderDialog = false }) {
                                                Text("Cancelar")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
