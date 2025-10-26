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
    onCancelSelection: () -> Unit
) {
    when {
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
                Button(onClick = onRequestPermissionClick) {
                    Text("Conceder permiso")
                }
            }
        }

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

        else -> {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = currentFolder ?: "Imágenes",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver atrás",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    // 🔹 Mostrar "Ver todas" solo si NO estamos dentro de una carpeta
                    actions = {
                        if (currentFolder == null) {
                            TextButton(onClick = onShowAllClick) {
                                Text("Ver todas", color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )


                ImageGrid(
                    images = images,
                    selectedImages = selectedImages,
                    onImageClick = onImageClick,
                    onSelectionChange = onSelectionChange,
                    onMoveSelectedClick = onMoveSelectedClick,
                    onCancelSelection = onCancelSelection
                )
            }
        }
    }
}
