package com.lucianolupo95.gallery.viewmodel

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val _images = MutableStateFlow<List<Uri>>(emptyList())
    val images = _images.asStateFlow()

    /** Carga todas las imágenes del dispositivo (más recientes primero). */
    fun loadImages() {
        val list = mutableListOf<Uri>()
        val cr = getApplication<Application>().contentResolver

        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        cr.query(collection, projection, null, null, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                list.add(uri)
            }
        }

        _images.value = list
    }

    /**
     * Elimina la imagen del MediaStore y actualiza el estado local.
     * Nota: En Android 11+ puede requerir consentimiento del usuario para ciertos archivos.
     */
}
