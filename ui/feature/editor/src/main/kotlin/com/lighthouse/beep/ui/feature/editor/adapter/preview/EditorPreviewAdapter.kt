package com.lighthouse.beep.ui.feature.editor.adapter.preview

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.lighthouse.beep.model.gallery.GalleryImage
import com.lighthouse.beep.model.gallery.GalleryImageDiff

internal class EditorPreviewAdapter(
    private val onEditorPreviewListener: OnEditorPreviewListener,
) : ListAdapter<GalleryImage, EditorPreviewViewHolder>(GalleryImageDiff()){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorPreviewViewHolder {
        return EditorPreviewViewHolder(parent, onEditorPreviewListener)
    }

    override fun onBindViewHolder(holder: EditorPreviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}