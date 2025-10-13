package com.lucianolupo95.gallery.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lucianolupo95.gallery.ui.components.ImageGrid
import com.lucianolupo95.gallery.ui.components.FolderGrid
import com.lucianolupo95.gallery.viewmodel.GalleryFolder

@Composable
fun MainScreen(
    hasPermission: Boolean,
    images: List<Uri>,
    onImageClick: (Int) -> Unit,
    onRequestPermissionClick: () -> Unit,
    folders: List<GalleryFolder>,
    onFolderClick: (String) -> Unit,
    onShowAllClick: () -> Unit,
    showFolders: Boolean,
    onToggleView: () -> Unit
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
            Column(Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = onShowAllClick, modifier = Modifier.padding(8.dp)) {
                        Text("Ver todas las fotos")
                    }
                }

                if (folders.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron carpetas 📂")
                    }
                } else {
                    FolderGrid(folders = folders, onFolderClick = { folderName ->
                        onFolderClick(folderName)
                    })
                }
            }
        }

        images.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No se encontraron imágenes 📷",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onToggleView) { Text("Volver a carpetas") }
                }
            }
        }

        else -> {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = onToggleView, modifier = Modifier.padding(8.dp)) {
                        Text("Volver a carpetas")
                    }
                }
                ImageGrid(images = images, onImageClick = onImageClick)
            }
        }
    }
}
