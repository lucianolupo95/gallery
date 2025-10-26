@file:Suppress("UNUSED_PARAMETER")

package com.lucianolupo95.gallery.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lucianolupo95.gallery.ui.components.FolderGrid
import com.lucianolupo95.gallery.ui.components.ImageGrid
import com.lucianolupo95.gallery.viewmodel.GalleryFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    hasPermission: Boolean,
    images: List<Uri>,
    selectedImages: List<Uri>,
    onImageClick: (Int) -> Unit,
    onRequestPermissionClick: () -> Unit,
    folders: List<GalleryFolder>,
    onFolderClick: (String) -> Unit,
    onShowAllClick: () -> Unit,
    showFolders: Boolean,
    onBackClick: () -> Unit,
    onCreateFolder: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onRenameFolder: (String, String) -> Unit,
    currentFolder: String? = null,
    onSelectionChange: (List<Uri>) -> Unit,
    onMoveSelectedClick: () -> Unit,
    onCancelSelection: () -> Unit,
    isSelectionMode: Boolean,
    onToggleSelectionMode: () -> Unit,
    onSelectionModeChange: (Boolean) -> Unit,
    isSdStorage: Boolean = false

) {
    // Creamos una copia local del modo selección para poder modificarlo
    var localSelectionMode = isSelectionMode

    when {
        // 🚫 Vista sin permisos
        !hasPermission -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🚫 Permisos no concedidos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Para acceder a tus imágenes, necesitamos tu permiso.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onRequestPermissionClick) {
                    Text("Conceder permiso")
                }
            }
        }

        // 📁 Vista de carpetas
        showFolders -> {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Carpetas") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                FolderGrid(
                    folders = folders,
                    onFolderClick = onFolderClick,
                    onCreateFolder = onCreateFolder,
                    onDeleteFolder = onDeleteFolder,
                    onRenameFolder = onRenameFolder
                )
            }
        }

        // 📷 Vista de imágenes
        else -> {
            Column {
                TopAppBar(
                    title = {
                        if (localSelectionMode) {
                            Text("${selectedImages.size} seleccionada(s)")
                        } else {
                            Text(currentFolder ?: "Imágenes")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver atrás"
                            )
                        }
                    },
                    actions = {
                        if (localSelectionMode) {
                            TextButton(onClick = onMoveSelectedClick) {
                                Text("Mover", color = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(onClick = onCancelSelection) {
                                Text("Cancelar", color = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            TextButton(onClick = onToggleSelectionMode) {
                                Text("Seleccionar", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )

                ImageGrid(
                    images = images,
                    selectedImages = selectedImages,
                    onImageClick = onImageClick,
                    onSelectionChange = {
                        onSelectionChange(it)
                        val mode = it.isNotEmpty()
                        localSelectionMode = mode
                        onSelectionModeChange(mode) // 👈 sincroniza con MainActivity
                    },
                    onMoveSelectedClick = onMoveSelectedClick,
                    onCancelSelection = {
                        localSelectionMode = false
                        onSelectionModeChange(false) // 👈 limpia el modo global
                        onCancelSelection()
                    }
                )

            }
        }
    }
}
