package com.lucianolupo95.gallery.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lucianolupo95.gallery.ui.components.ImageGrid

@Composable
fun MainScreen(
    hasPermission: Boolean,
    images: List<Uri>,
    onImageClick: (Int, List<Uri>) -> Unit, // 👈 ahora igual que ImageGrid
    onRequestPermissionClick: () -> Unit
) {
    if (!hasPermission) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🚫 Permisos no concedidos",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Para acceder a tus imágenes, necesitamos tu permiso.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRequestPermissionClick) {
                Text("Conceder permiso")
            }
        }
    } else {
        ImageGrid(images = images, onImageClick = onImageClick) // 👈 mismo tipo
    }
}
