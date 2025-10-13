package com.lucianolupo95.gallery

import android.app.RecoverableSecurityException
import android.content.IntentSender
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lucianolupo95.gallery.ui.MainScreen
import com.lucianolupo95.gallery.ui.ImageDetailScreen
import com.lucianolupo95.gallery.ui.theme.GalleryTheme
import com.lucianolupo95.gallery.viewmodel.GalleryViewModel
import com.lucianolupo95.gallery.util.PermissionManager
import kotlinx.coroutines.*

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
                val folders by viewModel.folders.collectAsState()
                var selectedIndex by remember { mutableStateOf<Int?>(null) }

                // 🔹 Estado actual
                var currentFolder by remember { mutableStateOf<String?>(null) }
                var showFolders by remember { mutableStateOf(true) }

                val scope = rememberCoroutineScope()

                // 🔹 Cargar contenido al conceder permisos
                LaunchedEffect(permissionManager.hasPermission.value) {
                    if (permissionManager.hasPermission.value) {
                        viewModel.loadFolders()
                        viewModel.loadImages()
                    }
                }

                // 🔹 Launcher para el borrado
                val deleteLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult()
                ) { _ ->
                    scope.launch(Dispatchers.Main) {
                        delay(300)
                        if (currentFolder == null) {
                            viewModel.loadImages()
                        } else {
                            viewModel.loadImagesFromFolder(currentFolder!!)
                        }
                        showFolders = false
                        Toast.makeText(
                            this@MainActivity,
                            "🗑️ Imagen eliminada (recargando galería)",
                            Toast.LENGTH_SHORT
                        ).show()
                        selectedIndex = null
                    }
                }

                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (selectedIndex != null) {
                        ImageDetailScreen(
                            imageUris = images,
                            startIndex = selectedIndex!!,
                            onBackClick = { selectedIndex = null },
                            onRequestDelete = { uri ->
                                scope.launch {
                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            val pendingIntent = MediaStore.createDeleteRequest(
                                                contentResolver,
                                                listOf(uri)
                                            )
                                            deleteLauncher.launch(
                                                IntentSenderRequest.Builder(
                                                    pendingIntent.intentSender
                                                ).build()
                                            )
                                        } else {
                                            withContext(Dispatchers.IO) {
                                                val deleted = contentResolver.delete(uri, null, null)
                                                withContext(Dispatchers.Main) {
                                                    if (deleted > 0) {
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            "🗑️ Imagen eliminada correctamente",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            "⚠️ No se pudo eliminar la imagen",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    selectedIndex = null
                                                    delay(300)
                                                    if (currentFolder == null) {
                                                        viewModel.loadImages()
                                                    } else {
                                                        viewModel.loadImagesFromFolder(currentFolder!!)
                                                    }
                                                    showFolders = false
                                                }
                                            }
                                        }
                                    } catch (e: RecoverableSecurityException) {
                                        val intentSender: IntentSender =
                                            e.userAction.actionIntent.intentSender
                                        deleteLauncher.launch(
                                            IntentSenderRequest.Builder(intentSender).build()
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        Toast.makeText(
                                            this@MainActivity,
                                            "⚠️ Error inesperado al eliminar",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        )
                    } else {
                        MainScreen(
                            hasPermission = permissionManager.hasPermission.value,
                            images = images,
                            onImageClick = { index -> selectedIndex = index },
                            onRequestPermissionClick = { permissionManager.requestPermission() },
                            folders = folders,
                            onFolderClick = { folderName ->
                                currentFolder = folderName
                                viewModel.loadImagesFromFolder(folderName)
                                showFolders = false
                            },
                            onShowAllClick = {
                                currentFolder = null
                                viewModel.loadImages()
                                showFolders = false // ⬅️ esto asegura el cambio de vista
                            },
                            showFolders = showFolders,
                            onToggleView = {
                                showFolders = !showFolders
                                if (showFolders) {
                                    viewModel.loadFolders()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
