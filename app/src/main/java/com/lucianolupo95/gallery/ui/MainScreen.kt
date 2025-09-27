package com.lucianolupo95.gallery.ui

import android.net.Uri
import androidx.compose.runtime.*
import com.lucianolupo95.gallery.ui.components.ImageGrid

@Composable
fun MainScreen() {
    var selectedImage by remember { mutableStateOf<Uri?>(null) }

    if (selectedImage == null) {
        ImageGrid(onImageClick = { uri -> selectedImage = uri })
    } else {
        ImageDetailScreen(
            imageUri = selectedImage!!,
            onBack = { selectedImage = null }
        )
    }
}
