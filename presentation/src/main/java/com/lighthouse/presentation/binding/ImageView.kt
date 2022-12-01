package com.lighthouse.presentation.binding

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.lighthouse.presentation.extension.getThumbnail

@BindingAdapter("loadUri")
fun ImageView.loadUri(uri: Uri?) {
    setImageBitmap(null)
    if (uri != null) {
        Glide.with(this)
            .load(uri)
            .into(this)
    } else {
        setImageBitmap(null)
    }
}

@BindingAdapter("loadUriWithoutCache")
fun ImageView.loadUriWithoutCache(uri: Uri?) {
    setImageBitmap(null)
    if (uri != null) {
        Glide.with(this)
            .load(uri)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(this)
    } else {
        setImageBitmap(null)
    }
}

@BindingAdapter("loadThumbnailByContentUri")
fun ImageView.loadThumbnailByContentUri(contentUri: Uri?) {
    val resolver = context.contentResolver
    val thumbnail = resolver.getThumbnail(contentUri)
    setImageBitmap(null)
    if (thumbnail != null) {
        Glide.with(this)
            .load(thumbnail)
            .centerCrop()
            .into(this)
    } else {
        if (contentUri != null) {
            Glide.with(this)
                .load(contentUri)
                .centerCrop()
                .into(this)
        } else {
            setImageBitmap(null)
        }
    }
}