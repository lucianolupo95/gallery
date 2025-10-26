package com.lucianolupo95.gallery.viewmodel

import android.net.Uri

data class GalleryFolder(
    val name: String,
    val path: String,
    val imageCount: Int = 0,
    val thumbnailUri: Uri? = null
) {
    companion object {
        @Suppress("unused")
        fun fromPath(folderPath: String, imageUris: List<Uri>): GalleryFolder {
            val name = folderPath.substringAfterLast('/')
            val count = imageUris.size
            val thumb = imageUris.lastOrNull()
            return GalleryFolder(
                name = name,
                path = folderPath,
                imageCount = count,
                thumbnailUri = thumb
            )
        }
    }
}
