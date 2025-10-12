package com.lucianolupo95.gallery.ui.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun ImageGrid(
    images: List<Uri>,
    onImageClick: (Int) -> Unit // 👈 solo índice
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        modifier = Modifier.padding(8.dp)
    ) {
        itemsIndexed(images) { index, uri ->
            Card(
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onImageClick(index) } // 👈 pasamos índice
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Imagen $index"
                )
            }
        }
    }
}
