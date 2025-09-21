package com.lucianolupo95.gallery.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.runtime.mutableStateOf

class PermissionManager(private val activity: ComponentActivity) {

    val hasPermission = mutableStateOf(false)

    private val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            hasPermission.value = isGranted
        }

    fun requestPermission() {
        val isAlreadyGranted =
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED

        if (isAlreadyGranted) {
            hasPermission.value = true
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }
}
