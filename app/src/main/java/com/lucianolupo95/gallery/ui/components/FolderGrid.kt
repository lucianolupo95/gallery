package com.lucianolupo95.gallery.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.lucianolupo95.gallery.viewmodel.GalleryFolder
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert

@Composable
fun FolderGrid(
    folders: List<GalleryFolder>,
    onFolderClick: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onDeleteFolder: (String) -> Unit,
    onRenameFolder: (String, String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    var folderToRename by remember { mutableStateOf<String?>(null) }
    var renameFolderName by remember { mutableStateOf("") }

    var folderToDelete by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {

        // 🔹 Botón "Nueva carpeta"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = { showCreateDialog = true }) {
                Text("＋ Nueva carpeta")
            }
        }

        // 🔹 Grid de carpetas
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
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
                    onDelete = {
                        folderToDelete = folder.name
                    }
                )
            }
        }

        // 🪄 Diálogo: Crear carpeta
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Crear nueva carpeta") },
                text = {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Nombre de carpeta") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newFolderName.isNotBlank()) {
                                onCreateFolder(newFolderName.trim())
                                newFolderName = ""
                                showCreateDialog = false
                            }
                        }
                    ) { Text("Crear") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
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
                    Text("¿Seguro que quieres eliminar la carpeta '$name'? Todas sus imágenes se borrarán permanentemente.")
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

// 🔹 Composable auxiliar: Tarjeta de carpeta con menú contextual
@Composable
private fun FolderCard(
    folder: GalleryFolder,
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            folder.thumbnailUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = folder.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentScale = ContentScale.Crop
                )
            }
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
                            style = MaterialTheme.typography.bodySmall
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
