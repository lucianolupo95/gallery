package com.lucianolupo95.gallery.util

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat

class PermissionManager(private val activity: Activity) {
    val hasPermission = mutableStateOf(false)

    fun requestPermission() {
        when {
            // ✅ Android 11+ (R / API 30 en adelante)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:" + activity.packageName)
                    activity.startActivity(intent)
                } else {
                    hasPermission.value = true
                }
            }

            // ✅ Android 10 o menor
            else -> {
                val permission = Manifest.permission.READ_EXTERNAL_STORAGE

                if (ContextCompat.checkSelfPermission(activity, permission)
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    hasPermission.value = true
                } else {
                    activity.requestPermissions(arrayOf(permission), 0)
                }
            }
        }
    }
}
