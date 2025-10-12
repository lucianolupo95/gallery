package com.lucianolupo95.gallery.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

fun shareImage(context: Context, imageUri: Uri) {
    try {
        // Copia temporal del archivo para compartir
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val cacheFile = File(context.cacheDir, "shared_image.jpg")
        val outputStream = FileOutputStream(cacheFile)

        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        // Uri accesible mediante FileProvider
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider", // Verifica que coincida con tu manifiesto
            cacheFile
        )

        // Intent de compartir
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Compartir imagen con...")
        )

    } catch (e: Exception) {
        e.printStackTrace()
    }
}
