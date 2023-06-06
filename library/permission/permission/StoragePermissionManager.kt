package com.lighthouse.core.android.utils.permission

import android.Manifest
import android.content.Context
import android.os.Build
import com.lighthouse.core.android.utils.permission.core.PermissionManager

class StoragePermissionManager(context: Context) : PermissionManager(context) {

    override val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
}