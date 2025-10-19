package com.lucianolupo95.gallery.viewmodel

import android.app.Application
import android.content.ContentUris
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

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

    private val logTag = "GalleryViewModel"

    // 🔹 Helper para mostrar errores
    private fun showToast(message: String) {
        Toast.makeText(getApplication(), message, Toast.LENGTH_LONG).show()
        Log.e(logTag, message)
    }

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

        Log.d(logTag, "Cargadas ${imageList.size} imágenes de ${folderName ?: "todas"}")
        _images.value = imageList
    }

    // 🔹 Cargar carpetas desde MediaStore
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

        Log.d(logTag, "Cargadas ${folderList.size} carpetas detectadas")
        _folders.value = folderList.sortedByDescending { it.imageCount }
        // 🔹 Buscar carpetas físicas en /Pictures que estén vacías y no aparezcan en MediaStore
        val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        picturesDir.listFiles()?.forEach { folder ->
            if (folder.isDirectory) {
                val exists = folderList.any { it.name == folder.name }
                if (!exists) {
                    folderList.add(GalleryFolder(folder.name, 0, null))
                }
            }
        }
    }

    fun createFolder(folderName: String): Boolean {
        return try {
            if (folderName.isBlank()) {
                Toast.makeText(getApplication(), "⚠️ Nombre de carpeta vacío", Toast.LENGTH_SHORT).show()
                return false
            }

            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val newFolder = File(picturesDir, folderName)

            if (newFolder.exists()) {
                Toast.makeText(getApplication(), "⚠️ Ya existe una carpeta con ese nombre", Toast.LENGTH_SHORT).show()
                return false
            }

            val created = newFolder.mkdirs()
            if (!created) {
                Toast.makeText(getApplication(), "⚠️ No se pudo crear la carpeta (permiso o restricción del sistema)", Toast.LENGTH_LONG).show()
                return false
            }

            // Forzar escaneo del sistema de medios
            MediaScannerConnection.scanFile(
                getApplication(),
                arrayOf(newFolder.absolutePath),
                null
            ) { path, uri ->
                Log.d("GalleryViewModel", "Escaneada carpeta: $path -> $uri")
            }

            Toast.makeText(getApplication(), "📁 Carpeta '$folderName' creada con éxito", Toast.LENGTH_SHORT).show()

            // Refrescar lista de carpetas en la UI
            loadFolders()

            true
        } catch (e: Exception) {
            Toast.makeText(getApplication(), "⚠️ Error al crear carpeta: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            Log.e("GalleryViewModel", "Error al crear carpeta", e)
            false
        }
    }


    // 🔹 Eliminar carpeta (borra físicamente)
    fun deleteFolder(folderName: String): Boolean {
        return try {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val folder = File(picturesDir, folderName)
            if (!folder.exists()) {
                showToast("⚠️ No se encontró la carpeta a eliminar")
                return false
            }

            folder.deleteRecursively()
            MediaScannerConnection.scanFile(getApplication(), arrayOf(folder.absolutePath), null, null)
            loadFolders()
            showToast("🗑️ Carpeta '$folderName' eliminada")
            true
        } catch (e: Exception) {
            showToast("⚠️ Error al eliminar carpeta: ${e.localizedMessage}")
            Log.e(logTag, "Error al eliminar carpeta", e)
            false
        }
    }

    // 🔹 Renombrar carpeta
    fun renameFolder(oldName: String, newName: String): Boolean {
        return try {
            val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val oldFolder = File(picturesDir, oldName)
            val newFolder = File(picturesDir, newName)

            if (!oldFolder.exists()) {
                showToast("⚠️ No se encontró la carpeta '$oldName'")
                return false
            }
            if (newFolder.exists()) {
                showToast("⚠️ Ya existe una carpeta llamada '$newName'")
                return false
            }

            val success = oldFolder.renameTo(newFolder)
            if (!success) {
                showToast("⚠️ No se pudo renombrar la carpeta (permiso o ruta denegada)")
                return false
            }

            MediaScannerConnection.scanFile(
                getApplication(),
                arrayOf(newFolder.absolutePath),
                null
            ) { path, _ -> Log.d(logTag, "Escaneado rename: $path") }

            loadFolders()
            showToast("✏️ Carpeta renombrada a '$newName'")
            true
        } catch (e: Exception) {
            showToast("⚠️ Error al renombrar carpeta: ${e.localizedMessage}")
            Log.e(logTag, "Error al renombrar carpeta", e)
            false
        }
    }
}
