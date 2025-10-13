package com.lucianolupo95.gallery.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.lucianolupo95.gallery.viewmodel.GalleryFolder

@Composable
fun FolderGrid(
    folders: List<GalleryFolder>,
    onFolderClick: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(folders.size) { index ->
            val folder = folders[index]
            Card(
                modifier = Modifier
                    .padding(6.dp)
                    .fillMaxWidth()
                    .clickable { onFolderClick(folder.name) },
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
                    Column(Modifier.padding(8.dp)) {
                        Text(folder.name, style = MaterialTheme.typography.bodyLarge)
                        Text("${folder.imageCount} imágenes", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
