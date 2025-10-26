package com.lucianolupo95.gallery

import android.app.RecoverableSecurityException
import android.content.IntentSender
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lucianolupo95.gallery.ui.ImageDetailScreen
import com.lucianolupo95.gallery.ui.MainScreen
import com.lucianolupo95.gallery.ui.theme.GalleryTheme
import com.lucianolupo95.gallery.util.PermissionManager
import com.lucianolupo95.gallery.viewmodel.GalleryViewModel
import kotlinx.coroutines.*

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)
        permissionManager.requestPermission()

        setContent {
            var isSelectionMode by remember { mutableStateOf(false) }

            GalleryTheme {
                val viewModel: GalleryViewModel by viewModels()
                val images by viewModel.images.collectAsState()
                val folders by viewModel.folders.collectAsState()

                var selectedIndex by remember { mutableStateOf<Int?>(null) }
                var selectedImages by remember { mutableStateOf<List<android.net.Uri>>(emptyList()) }

                var currentFolder by remember { mutableStateOf<String?>(null) }
                var showFolders by remember { mutableStateOf(true) }
                var showMoveDialog by remember { mutableStateOf(false) }

                val scope = rememberCoroutineScope()

                // Cargar datos tras permisos
                LaunchedEffect(permissionManager.hasPermission.value) {
                    if (permissionManager.hasPermission.value) {
                        viewModel.loadFolders()
                        viewModel.loadImages()
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
                        Toast.makeText(
                            this@MainActivity,
                            "🗑️ Imagen eliminada (recargando galería)",
                            Toast.LENGTH_SHORT
                        ).show()
                        selectedIndex = null
                    }
                }

                Surface(modifier = Modifier, color = MaterialTheme.colorScheme.background) {
                    when {
                        // Vista de detalle
                        selectedIndex != null -> {
                            ImageDetailScreen(
                                imageUris = images,
                                startIndex = selectedIndex!!,
                                onBackClick = { selectedIndex = null },
                                onRequestDelete = { uri ->
                                    scope.launch {
                                        try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                                val pendingIntent = MediaStore.createDeleteRequest(
                                                    contentResolver,
                                                    listOf(uri)
                                                )
                                                deleteLauncher.launch(
                                                    IntentSenderRequest.Builder(
                                                        pendingIntent.intentSender
                                                    ).build()
                                                )
                                            } else {
                                                withContext(Dispatchers.IO) {
                                                    val deleted = contentResolver.delete(uri, null, null)
                                                    withContext(Dispatchers.Main) {
                                                        if (deleted > 0) {
                                                            Toast.makeText(
                                                                this@MainActivity,
                                                                "🗑️ Imagen eliminada correctamente",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        } else {
                                                            Toast.makeText(
                                                                this@MainActivity,
                                                                "⚠️ No se pudo eliminar la imagen",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                        selectedIndex = null
                                                        delay(300)
                                                        if (currentFolder == null)
                                                            viewModel.loadImages()
                                                        else
                                                            viewModel.loadImagesFromFolder(currentFolder!!)
                                                    }
                                                }
                                            }
                                        } catch (e: RecoverableSecurityException) {
                                            val intentSender: IntentSender =
                                                e.userAction.actionIntent.intentSender
                                            deleteLauncher.launch(
                                                IntentSenderRequest.Builder(intentSender).build()
                                            )
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                            Toast.makeText(
                                                this@MainActivity,
                                                "⚠️ Error inesperado al eliminar",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            )
                        }

                        // Vista normal (carpetas o galería)
                        else -> {
                            MainScreen(
                                hasPermission = permissionManager.hasPermission.value,
                                images = images,
                                selectedImages = selectedImages,
                                onImageClick = { index ->
                                    if (isSelectionMode) {
                                        // Selección directa si ya hay selección
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
                                onFolderClick = { folderName ->
                                    currentFolder = folderName
                                    showFolders = false
                                    viewModel.loadImagesFromFolder(folderName)
                                    selectedImages = emptyList()
                                    isSelectionMode = false
                                },
                                onShowAllClick = {
                                    currentFolder = null
                                    viewModel.loadImages()
                                    showFolders = false
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
                                    val success = viewModel.createFolder(name)
                                    Toast.makeText(
                                        this,
                                        if (success) "📁 Carpeta '$name' creada"
                                        else "⚠️ Error al crear carpeta",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onDeleteFolder = { name ->
                                    val success = viewModel.deleteFolder(name)
                                    Toast.makeText(
                                        this,
                                        if (success) "🗑️ Carpeta '$name' eliminada"
                                        else "⚠️ No se pudo eliminar carpeta",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onRenameFolder = { oldName, newName ->
                                    val success = viewModel.renameFolder(oldName, newName)
                                    Toast.makeText(
                                        this,
                                        if (success) "✏️ Carpeta renombrada"
                                        else "⚠️ Error al renombrar carpeta",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                currentFolder = currentFolder,
                                onSelectionChange = { selectedImages = it },
                                onMoveSelectedClick = {
                                    if (selectedImages.isNotEmpty()) showMoveDialog = true
                                },
                                onCancelSelection = {
                                    selectedImages = emptyList()
                                    isSelectionMode = false
                                },
                                isSelectionMode = isSelectionMode,
                                onToggleSelectionMode = { isSelectionMode = !isSelectionMode },
                                onSelectionModeChange = { mode -> isSelectionMode = mode }
                            )

                            // 🔹 Diálogo de "Mover a carpeta"
                            if (showMoveDialog && selectedImages.isNotEmpty()) {
                                AlertDialog(
                                    onDismissRequest = { showMoveDialog = false },
                                    title = { Text("Mover a carpeta") },
                                    text = {
                                        Column {
                                            Text("Seleccioná la carpeta de destino:")
                                            Spacer(modifier = Modifier.height(8.dp))
                                            folders.forEach { folder ->
                                                TextButton(onClick = {
                                                    scope.launch {
                                                        val success = viewModel.moveImagesToFolder(
                                                            selectedImages, folder.name
                                                        )
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            if (success)
                                                                "📦 Imágenes movidas a '${folder.name}'"
                                                            else
                                                                "⚠️ Error al mover imágenes",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        // Cerrar diálogo y limpiar selección/modo
                                                        showMoveDialog = false
                                                        selectedImages = emptyList()
                                                        isSelectionMode = false

                                                        // Refrescar vista actual SIN cambiar de carpeta
                                                        if (currentFolder == null)
                                                            viewModel.loadImages()
                                                        else
                                                            viewModel.loadImagesFromFolder(currentFolder!!)

                                                        viewModel.loadFolders()
                                                    }
                                                }) {
                                                    Text(folder.name)
                                                }
                                            }
                                        }
                                    },
                                    confirmButton = {
                                        TextButton(onClick = { showMoveDialog = false }) {
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
