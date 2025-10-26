package com.lucianolupo95.gallery.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lucianolupo95.gallery.ui.components.ImageGrid
import com.lucianolupo95.gallery.ui.components.FolderGrid
import com.lucianolupo95.gallery.viewmodel.GalleryFolder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    hasPermission: Boolean,
    images: List<Uri>,
    selectedImages: List<Uri>, // ✅ agregado
    onImageClick: (Int) -> Unit,
    onRequestPermissionClick: () -> Unit,
    folders: List<GalleryFolder>,
    onFolderClick: (String) -> Unit,
    onShowAllClick: () -> Unit,
    showFolders: Boolean,
    onToggleView: () -> Unit,
    // ⬇️ Passthroughs
    onCreateFolder: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onRenameFolder: (String, String) -> Unit,
    // Para mostrar “← Atrás” con el nombre de carpeta
    currentFolder: String? = null,
    // ✅ Callbacks de selección múltiple
    onSelectionChange: (List<Uri>) -> Unit,
    onMoveSelectedClick: () -> Unit,
    onCancelSelection: () -> Unit
) {
    when {
        !hasPermission -> {
            // 🚫 Sin permisos
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

        showFolders -> {
            // 📁 Vista de carpetas
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Carpetas") },
                    actions = {
                        TextButton(onClick = onShowAllClick) { Text("Ver todas las fotos") }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
            // 📷 Vista de galería
            Column {
                if (currentFolder != null) {
                    TopAppBar(
                        title = { Text(currentFolder) },
                        navigationIcon = {
                            TextButton(onClick = { onToggleView() }) { Text("← Atrás") }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }

                // ✅ Mostrar galería
                ImageGrid(
                    images = images,
                    selectedImages = selectedImages, // 👈 agregado
                    onImageClick = onImageClick,
                    onSelectionChange = onSelectionChange,
                    onMoveSelectedClick = onMoveSelectedClick,
                    onCancelSelection = onCancelSelection
                )
            }
        }
    }
}
