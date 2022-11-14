package com.lighthouse.presentation.model

import android.net.Uri
import java.util.Date

sealed class GalleryUIModel {
    data class Header(val date: String) : GalleryUIModel()
    data class Gallery(
        val id: Long,
        val uri: Uri,
        val date: Date
    ) : GalleryUIModel()
}
