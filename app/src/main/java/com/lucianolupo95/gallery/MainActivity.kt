package com.lucianolupo95.gallery

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lucianolupo95.gallery.ui.MainScreen
import com.lucianolupo95.gallery.ui.ImageDetailScreen
import com.lucianolupo95.gallery.ui.theme.GalleryTheme
import com.lucianolupo95.gallery.util.PermissionManager
import com.lucianolupo95.gallery.viewmodel.GalleryViewModel

class MainActivity : ComponentActivity() {

    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)
        permissionManager.requestPermission()

        setContent {
            GalleryTheme {
                val viewModel: GalleryViewModel by viewModels()
                val images by viewModel.images.collectAsState()
                var selectedImageIndex by remember { mutableStateOf<Int?>(null) }

                LaunchedEffect(permissionManager.hasPermission.value) {
                    if (permissionManager.hasPermission.value) {
                        viewModel.rescanImages(this@MainActivity)
                        viewModel.loadImages()
                    }
                }

                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    selectedImageIndex?.let { index ->
                        ImageDetailScreen(
                            imageUris = images,
                            startIndex = index,
                            onBackClick = { selectedImageIndex = null } // 👈 correcto
                        )
                    } ?: MainScreen(
                        hasPermission = permissionManager.hasPermission.value,
                        images = images,
                        onImageClick = { index, list -> selectedImageIndex = index }, // 👈 ajustado
                        onRequestPermissionClick = {
                            permissionManager.requestPermission()
                        }
                    )
                }
            }
        }
    }
}
