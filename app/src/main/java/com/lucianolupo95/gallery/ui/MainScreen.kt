package com.lucianolupo95.gallery.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lucianolupo95.gallery.ui.components.ImageGrid


@Composable
fun MainScreen(
    hasPermission: Boolean,
    images: List<Uri>,
    onRequestPermissionClick: () -> Unit
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

        if (hasPermission) {
            if (images.isNotEmpty()) {
                Text("Cantidad de imágenes: ${images.size}")
                Spacer(modifier = Modifier.height(8.dp))
                ImageGrid(images = images)
            } else {
                Text("No se encontraron imágenes.")
            }
        } else {
            Button(onClick = onRequestPermissionClick) {
                Text("Solicitar permiso")
            }
        }
    }
}

