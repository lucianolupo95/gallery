package com.lucianolupo95.gallery.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.lucianolupo95.gallery.viewmodel.GalleryFolder // ✅ import correcto

@Composable
fun FolderGrid(
    folders: List<GalleryFolder>, // ✅ clase, no archivo
    onFolderClick: (String) -> Unit,
    onCreateFolder: (String) -> Unit, // se mantiene para compatibilidad
    onDeleteFolder: (String) -> Unit,
    onRenameFolder: (String, String) -> Unit,
    isSdStorage: Boolean = false // ✅ banner informativo
) {
    var folderToRename by remember { mutableStateOf<String?>(null) }
    var renameFolderName by remember { mutableStateOf("") }
    var folderToDelete by remember { mutableStateOf<String?>(null) }
    val hasSd = folders.any { it.path.startsWith("content://") }
    val bannerText = when {
        hasSd -> "📂 Carpetas combinadas (SD + interna)"
        isSdStorage -> "📂 Carpetas (tarjeta SD)"
        else -> "📂 Carpetas (memoria interna)"
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // 🔹 Banner superior informativo
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isSdStorage)
                    "📂 Carpetas (almacenadas en tarjeta SD)"
                else
                    "📂 Carpetas (almacenadas en memoria interna)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(12.dp)
            )
        }

        // 🔹 Grid de carpetas
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(folders) { folder ->
                FolderCard(
                    folder = folder,
                    onClick = { onFolderClick(folder.name) },
                    onRename = {
                        folderToRename = folder.name
                        renameFolderName = folder.name
                    },
                    onDelete = { folderToDelete = folder.name }
                )
            }
        }

        // 🪄 Diálogo: Renombrar carpeta
        folderToRename?.let { oldName ->
            AlertDialog(
                onDismissRequest = { folderToRename = null },
                title = { Text("Renombrar carpeta") },
                text = {
                    OutlinedTextField(
                        value = renameFolderName,
                        onValueChange = { renameFolderName = it },
                        label = { Text("Nuevo nombre") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (renameFolderName.isNotBlank()) {
                                onRenameFolder(oldName, renameFolderName.trim())
                                folderToRename = null
                            }
                        }
                    ) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { folderToRename = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // 🪄 Diálogo: Confirmar eliminación
        folderToDelete?.let { name ->
            AlertDialog(
                onDismissRequest = { folderToDelete = null },
                title = { Text("Eliminar carpeta") },
                text = {
                    Text("¿Seguro que querés eliminar la carpeta '$name'? Todas sus imágenes se borrarán permanentemente.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onDeleteFolder(name)
                            folderToDelete = null
                        }
                    ) { Text("Eliminar") }
                },
                dismissButton = {
                    TextButton(onClick = { folderToDelete = null }) {
                        Text("Cancelar")
                    }
                },
                icon = { Icon(Icons.Default.Delete, contentDescription = null) }
            )
        }
    }
}

// 🔹 Tarjeta de carpeta (con miniatura o ícono fallback)
@Composable
private fun FolderCard(
    folder: GalleryFolder, // ✅ clase correcta
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .padding(6.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // 🖼️ Miniatura o ícono genérico
            if (folder.thumbnailUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(folder.thumbnailUri),
                    contentDescription = folder.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Folder,
                        contentDescription = "Carpeta vacía",
                        tint = Color(0xFF777777),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            // 📋 Nombre + contador + menú contextual
            Column(
                Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            folder.name,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1
                        )
                        Text(
                            "${folder.imageCount} imágenes",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Opciones")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Renombrar") },
                                onClick = {
                                    expanded = false
                                    onRename()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar") },
                                onClick = {
                                    expanded = false
                                    onDelete()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
