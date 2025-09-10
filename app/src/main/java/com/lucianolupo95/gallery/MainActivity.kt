package com.lucianolupo95.gallery

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lucianolupo95.gallery.ui.theme.GalleryTheme

class MainActivity : ComponentActivity() {
    private lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this)
        permissionManager.requestPermission()

        val viewModel: GalleryViewModel by viewModels()

        setContent {
            GalleryTheme {
                val images by viewModel.images.collectAsState()

                LaunchedEffect(permissionManager.hasPermission.value) {
                    if (permissionManager.hasPermission.value) {
                        viewModel.loadImages()
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        hasPermission = permissionManager.hasPermission.value,
                        images = images
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    hasPermission: Boolean,
    images: List<Uri>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Galería de Mamá",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (hasPermission) "✅ Permiso CONCEDIDO" else "❌ Permiso NO concedido",
            style = MaterialTheme.typography.bodyMedium,
            color = if (hasPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Cantidad de imágenes: ${images.size}")
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    GalleryTheme {
        MainScreen(
            hasPermission = false,
            images = emptyList()
        )
    }
}
