package com.lucianolupo95.gallery.viewmodel

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream

class GalleryViewModel(application: Application) : AndroidViewModel(application) {

    private val _images = MutableStateFlow<List<Uri>>(emptyList())
    val images = _images.asStateFlow()

    private val _folders = MutableStateFlow<List<GalleryFolder>>(emptyList())
    val folders = _folders.asStateFlow()

    private val context: Context get() = getApplication<Application>().applicationContext
    var sdCardUri: Uri? = null

    // ------------------ Helpers ------------------

    private fun openInputStreamSmart(uri: Uri): InputStream? =
        when (uri.scheme) {
            "content" -> context.contentResolver.openInputStream(uri)
            "file" -> uri.path?.let { FileInputStream(File(it)) }
            else -> null
        }

    private fun getDisplayNameSmart(uri: Uri): String =
        when (uri.scheme) {
            "content" -> {
                var name: String? = null
                context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use {
                    if (it.moveToFirst()) name = it.getString(0)
                }
                name ?: "imagen.jpg"
            }
            "file" -> File(uri.path ?: "").name.ifBlank { "imagen.jpg" }
            else -> "imagen.jpg"
        }

    private fun scanPaths(vararg paths: String) {
        if (paths.isNotEmpty())
            MediaScannerConnection.scanFile(context, paths, null, null)
    }

    // ------------------ Carpetas ------------------

    fun loadFolders() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = mutableListOf<GalleryFolder>()

            // Interno
            val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val internal = pictures.listFiles()
                ?.filter { it.isDirectory && !it.name.startsWith(".") && !it.name.equals("thumbnails", true) }
                ?.mapNotNull { dir ->
                    val imgs = dir.listFiles()
                        ?.filter { it.isFile && it.extension.lowercase() in listOf("jpg","jpeg","png","webp") }
                        ?.sortedByDescending { it.lastModified() }
                        ?: emptyList()
                    if (imgs.isEmpty()) null else GalleryFolder(
                        name = dir.name,
                        path = dir.absolutePath,
                        thumbnailUri = Uri.fromFile(imgs.first()),
                        imageCount = imgs.size
                    )
                } ?: emptyList()
            result += internal

            // SD (SAF)
            sdCardUri?.let { tree ->
                val root = DocumentFile.fromTreeUri(context, tree)
                root?.listFiles()
                    ?.filter { it.isDirectory && it.name?.startsWith(".") != true }
                    ?.forEach { dir ->
                        val images = dir.listFiles()
                            .filter { d -> d.isFile && (d.name?.substringAfterLast('.')?.lowercase() in listOf("jpg","jpeg","png","webp")) }
                        if (images.isNotEmpty()) {
                            val thumb = images.firstOrNull()
                            result += GalleryFolder(
                                name = dir.name ?: "(sin nombre)",
                                path = dir.uri.toString(),
                                thumbnailUri = thumb?.uri,
                                imageCount = images.size
                            )
                        }
                    }
            }

            _folders.value = result.sortedBy { it.name.lowercase() }
        }
    }

    // ------------------ Imágenes ------------------

    fun loadImages() {
        viewModelScope.launch(Dispatchers.IO) {
            val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val list = pictures
                .walkTopDown()
                .filter { it.isFile && it.extension.lowercase() in listOf("jpg","jpeg","png","webp") }
                .sortedByDescending { it.lastModified() }
                .map { Uri.fromFile(it) }
                .toList()
            _images.value = list
        }
    }

    fun loadImagesFromFolder(folderName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName)
            val list = folder.listFiles()
                ?.filter { it.isFile && it.extension.lowercase() in listOf("jpg","jpeg","png","webp") }
                ?.sortedByDescending { it.lastModified() }
                ?.map { Uri.fromFile(it) }
                ?: emptyList()
            _images.value = list
        }
    }

    // ------------------ Carpetas CRUD ------------------

    fun createFolder(name: String): Boolean = try {
        if (sdCardUri != null) {
            val root = DocumentFile.fromTreeUri(context, sdCardUri!!)
            root?.findFile(name) ?: root?.createDirectory(name)
            true
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name)
            if (!dir.exists()) dir.mkdirs() else true
        }
    } catch (e: Exception) {
        e.printStackTrace(); false
    }

    fun deleteFolder(name: String): Boolean = try {
        val folder = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), name)
        if (folder.exists()) {
            folder.deleteRecursively()
            scanPaths(folder.absolutePath)
            true
        } else false
    } catch (e: Exception) {
        e.printStackTrace(); false
    }

    fun renameFolder(oldName: String, newName: String): Boolean = try {
        val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val old = File(pictures, oldName)
        val neu = File(pictures, newName)
        val ok = old.renameTo(neu)
        if (ok) scanPaths(neu.absolutePath)
        ok
    } catch (e: Exception) {
        e.printStackTrace(); false
    }

    // ------------------ Mover imágenes ------------------

    fun moveImagesToFolder(selectedUris: List<Uri>, folderName: String): Boolean {
        return try {
            val cr: ContentResolver = context.contentResolver

            if (sdCardUri != null) {
                // --- Destino en SD ---
                val root = DocumentFile.fromTreeUri(context, sdCardUri!!) ?: return false
                val target = root.findFile(folderName) ?: root.createDirectory(folderName) ?: return false

                for (src in selectedUris) {
                    val fileName = getDisplayNameSmart(src)
                    val inStream: InputStream = openInputStreamSmart(src) ?: continue

                    val mime = when (fileName.substringAfterLast('.', "").lowercase()) {
                        "png" -> "image/png"
                        "webp" -> "image/webp"
                        else -> "image/jpeg"
                    }

                    val newDoc = target.createFile(mime, fileName) ?: continue
                    val outStream: OutputStream = cr.openOutputStream(newDoc.uri) ?: continue

                    inStream.use { input -> outStream.use { output -> input.copyTo(output) } }

                    // borrar origen
                    try { cr.delete(src, null, null) } catch (_: Exception) {
                        if (src.scheme == "file") File(src.path ?: "").delete()
                    }

                    scanPaths(fileName)
                }

                // Refresca listas
                loadFolders()
                loadImagesFromFolder(folderName)
                true

            } else {
                // --- Destino interno ---
                val pictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val dstDir = File(pictures, folderName)
                if (!dstDir.exists()) dstDir.mkdirs()

                for (src in selectedUris) {
                    val srcFile = if (src.scheme == "file") File(src.path ?: "") else null
                    val fileName = srcFile?.name ?: getDisplayNameSmart(src)
                    val dstFile = File(dstDir, fileName)

                    if (srcFile != null && srcFile.exists()) {
                        val moved = srcFile.renameTo(dstFile)
                        if (!moved) {
                            srcFile.copyTo(dstFile, overwrite = true)
                            srcFile.delete()
                        }
                        scanPaths(srcFile.absolutePath, dstFile.absolutePath)
                    } else {
                        val inStream = cr.openInputStream(src) ?: continue
                        dstFile.outputStream().use { out -> inStream.use { inp -> inp.copyTo(out) } }
                        try { cr.delete(src, null, null) } catch (_: Exception) {}
                        scanPaths(dstFile.absolutePath)
                    }
                }

                loadFolders()
                loadImagesFromFolder(folderName)
                true
            }

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
