package com.lucianolupo95.gallery.ui

import android.net.Uri
import androidx.compose.runtime.*
import com.lucianolupo95.gallery.ui.components.ImageGrid

@Composable
fun MainScreen() {
    var imageList by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    if (selectedIndex == null) {
        ImageGrid(
            onImageClick = { index, uris ->
                imageList = uris
                selectedIndex = index
            }
        )
    } else {
        ImageDetailScreen(
            imageUris = imageList,
            startIndex = selectedIndex!!,
            onBack = { selectedIndex = null }
        )
    }
}
