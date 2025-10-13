package com.lucianolupo95.gallery.viewmodel

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class GalleryFolder(
    val name: String,
    val imageCount: Int,
    val thumbnailUri: Uri?
)

class GalleryViewModel(app: Application) : AndroidViewModel(app) {

    private val _images = MutableStateFlow<List<Uri>>(emptyList())
    val images = _images.asStateFlow()

    private val _folders = MutableStateFlow<List<GalleryFolder>>(emptyList())
    val folders = _folders.asStateFlow()

    // 🔹 Cargar imágenes (todas)
    fun loadImages() {
        loadImagesFromFolder(null)
    }

    // 🔹 Cargar imágenes por carpeta
    fun loadImagesFromFolder(folderName: String?) {
        val imageList = mutableListOf<Uri>()
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )

        val selection = if (folderName != null)
            "${MediaStore.Images.Media.BUCKET_DISPLAY_NAME} = ?"
        else null
        val selectionArgs = if (folderName != null) arrayOf(folderName) else null
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        getApplication<Application>().contentResolver.query(
            collection, projection, selection, selectionArgs, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = ContentUris.withAppendedId(collection, id)
                imageList.add(uri)
            }
        }
        _images.value = imageList
    }

    // 🔹 Cargar carpetas con miniatura y cantidad
    fun loadFolders() {
        val folderList = mutableListOf<GalleryFolder>()
        val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val bucketColumn = MediaStore.Images.Media.BUCKET_DISPLAY_NAME

        val foldersMap = mutableMapOf<String, Pair<Int, Uri?>>()

        getApplication<Application>().contentResolver.query(
            collection, arrayOf(
                MediaStore.Images.Media._ID,
                bucketColumn
            ), null, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val bucketCol = cursor.getColumnIndexOrThrow(bucketColumn)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val bucket = cursor.getString(bucketCol) ?: "Desconocida"
                if (!foldersMap.containsKey(bucket)) {
                    val uri = ContentUris.withAppendedId(collection, id)
                    foldersMap[bucket] = Pair(1, uri)
                } else {
                    val (count, thumb) = foldersMap[bucket]!!
                    foldersMap[bucket] = Pair(count + 1, thumb)
                }
            }
        }

        foldersMap.forEach { (name, data) ->
            folderList.add(GalleryFolder(name, data.first, data.second))
        }

        _folders.value = folderList.sortedByDescending { it.imageCount }
    }
}
