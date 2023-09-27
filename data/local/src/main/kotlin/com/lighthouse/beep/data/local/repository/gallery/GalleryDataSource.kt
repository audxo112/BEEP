package com.lighthouse.beep.data.local.repository.gallery

import android.content.ContentResolver
import android.content.ContentUris
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import com.lighthouse.beep.model.gallery.GalleryImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

internal class GalleryDataSource @Inject constructor(
    private val contentResolver: ContentResolver,
) {
    suspend fun getImages(page: Int, limit: Int): List<GalleryImage> {
        return getImages(null, null, page, limit)
    }

    private suspend fun getImages(
        selection: String?,
        selectionArg: Array<String>?,
        page: Int,
        limit: Int,
    ): List<GalleryImage> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATA,
        )
//        val selection = "${MediaStore.Images.Media.BUCKET_ID}=$bucketId AND ${MediaStore.Images.Media.MIME_TYPE} in (?,?)"
//        val mimeTypeMap = MimeTypeMap.getSingleton()
//        val selectionArg = arrayOf(
//            mimeTypeMap.getMimeTypeFromExtension("png"),
//            mimeTypeMap.getMimeTypeFromExtension("jpg"),
//        )
        val offset = page * limit
        val cursor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val queryArgs = Bundle().apply {
                putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
                putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArg)
                putStringArray(
                    ContentResolver.QUERY_ARG_SORT_COLUMNS,
                    arrayOf(MediaStore.Images.Media.DATE_ADDED),
                )
                putInt(
                    ContentResolver.QUERY_ARG_SORT_DIRECTION,
                    ContentResolver.QUERY_SORT_DIRECTION_DESCENDING,
                )
                putInt(ContentResolver.QUERY_ARG_OFFSET, offset)
                putInt(ContentResolver.QUERY_ARG_LIMIT, limit)
            }
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                queryArgs,
                null,
            )
        } else {
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC LIMIT $limit OFFSET $offset"
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArg,
                sortOrder,
            )
        }

        val list = ArrayList<GalleryImage>()
        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val imagePathColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val dateAddedColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val contentUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                val dateAdded = it.getLong(dateAddedColumn) * 1000
                val imagePath = it.getString(imagePathColumn)
                list.add(GalleryImage(id, contentUri, imagePath, Date(dateAdded)))
            }
        }
        return@withContext list
    }

    fun getImageSize(): Int {
        return contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            null,
        )?.use {
            it.count
        } ?: 0
    }
}
