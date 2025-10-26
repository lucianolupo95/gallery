package com.lucianolupo95.gallery.ui.components

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ImageGrid(
    images: List<Uri>,
    selectedImages: List<Uri>,
    onImageClick: (Int) -> Unit,
    onSelectionChange: (List<Uri>) -> Unit,
    onMoveSelectedClick: () -> Unit,
    onCancelSelection: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 🔹 Barra superior en modo selección
        if (selectedImages.isNotEmpty()) {
            TopAppBar(
                title = { Text("${selectedImages.size} seleccionada(s)") },
                actions = {
                    TextButton(onClick = onMoveSelectedClick) {
                        Text("Mover", color = MaterialTheme.colorScheme.primary)
                    }
                    TextButton(onClick = onCancelSelection) {
                        Text("Cancelar", color = MaterialTheme.colorScheme.error)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }

        // 🔹 Grid principal
        LazyVerticalGrid(
            columns = GridCells.Adaptive(120.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentPadding = PaddingValues(4.dp)
        ) {
            itemsIndexed(images) { index, uri ->
                val isSelected = selectedImages.contains(uri)

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .aspectRatio(1f)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .combinedClickable(
                            onClick = {
                                if (selectedImages.isNotEmpty()) {
                                    val updated = if (isSelected) {
                                        selectedImages - uri
                                    } else {
                                        selectedImages + uri
                                    }
                                    onSelectionChange(updated)
                                } else {
                                    onImageClick(index)
                                }
                            },
                            onLongClick = {
                                if (selectedImages.isEmpty()) {
                                    onSelectionChange(listOf(uri))
                                }
                            }
                        )
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(uri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }
    }
}
