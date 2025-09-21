package com.lucianolupo95.gallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lucianolupo95.gallery.ui.theme.GalleryTheme
import com.lucianolupo95.gallery.viewmodel.GalleryViewModel
import com.lucianolupo95.gallery.ui.MainScreen
import com.lucianolupo95.gallery.util.PermissionManager


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

                // Si cambia el permiso, actualiza las imágenes
                LaunchedEffect(permissionManager.hasPermission.value) {
                    if (permissionManager.hasPermission.value) {
                        viewModel.rescanImages(this@MainActivity) // ← 👈 esto lo agregás
                        viewModel.loadImages()
                    }
                }


                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        hasPermission = permissionManager.hasPermission.value,
                        images = images,
                        onRequestPermissionClick = {
                            permissionManager.requestPermission()
                        }
                    )
                }
            }
        }
    }
}
