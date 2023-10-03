package com.lighthouse.beep.ui.feature.editor.page.preview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.RectF
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import coil.load
import coil.size.Size
import com.lighthouse.beep.core.common.exts.cast
import com.lighthouse.beep.core.common.exts.dp
import com.lighthouse.beep.core.ui.binding.viewBindings
import com.lighthouse.beep.core.ui.exts.createThrottleClickListener
import com.lighthouse.beep.core.ui.exts.repeatOnStarted
import com.lighthouse.beep.ui.feature.editor.EditorSelectGifticonDataDelegate
import com.lighthouse.beep.ui.feature.editor.OnDialogProvider
import com.lighthouse.beep.ui.feature.editor.EditorViewModel
import com.lighthouse.beep.ui.feature.editor.OnEditorChipListener
import com.lighthouse.beep.ui.feature.editor.R
import com.lighthouse.beep.ui.feature.editor.databinding.FragmentEditorPreviewBinding
import com.lighthouse.beep.ui.feature.editor.model.EditType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class EditorPreviewFragment : Fragment(R.layout.fragment_editor_preview) {

    companion object {
        private val VIEW_RECT = RectF(0f, 0f, 80f.dp, 80f.dp)
    }

    private val delegate: EditorSelectGifticonDataDelegate by activityViewModels<EditorViewModel>()

    private val viewModel by viewModels<EditorPreviewViewModel>(
        factoryProducer = {
            EditorPreviewViewModel.factory(delegate)
        }
    )

    private val binding by viewBindings<FragmentEditorPreviewBinding>()

    private lateinit var onDialogProvider: OnDialogProvider
    private lateinit var onEditorChipListener: OnEditorChipListener

    override fun onAttach(context: Context) {
        super.onAttach(context)

        onDialogProvider = context.cast()
        onEditorChipListener = context.cast()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpPreview()
        setUpCollectState()
        setUpOnClickEvent()
    }

    private fun setUpPreview() {
        binding.imageThumbnail.clipToOutline = true
    }

    @SuppressLint("SetTextI18n")
    private fun setUpCollectState() {
        repeatOnStarted {
            viewModel.thumbnailUri.collect { contentUri ->
                binding.imageThumbnail.load(contentUri) {
                    size(Size.ORIGINAL)
                }
            }
        }

        repeatOnStarted {
            viewModel.thumbnailCropData.collect { data ->
                if (data.isCropped) {
                    binding.imageThumbnail.scaleType = ImageView.ScaleType.MATRIX
                    binding.imageThumbnail.imageMatrix = data.calculateMatrix(VIEW_RECT)
                } else {
                    binding.imageThumbnail.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            }
        }

        repeatOnStarted {
            viewModel.gifticonName.collect { name ->
                binding.iconNameEmpty.isVisible = name.isEmpty()
                binding.textName.text = name
            }
        }

        repeatOnStarted {
            viewModel.brandName.collect { brand ->
                binding.iconBrandEmpty.isVisible = brand.isEmpty()
                binding.textBrand.text = brand
            }
        }

        repeatOnStarted {
            viewModel.expired.collect { expired ->
                binding.iconExpiredEmpty.isVisible = expired.isEmpty()
                binding.textExpired.text = expired
            }
        }

        repeatOnStarted {
            viewModel.memo.collect { memo ->
                binding.textMemo.text = memo
                binding.textMemoLength.text = "${memo.length}/${EditType.MEMO.maxLength}"
            }
        }
    }

    private fun setUpOnClickEvent() {
        binding.imageThumbnail.setOnClickListener(createThrottleClickListener {
            onEditorChipListener.selectEditorChip(EditType.THUMBNAIL)
        })

        binding.textName.setOnClickListener(createThrottleClickListener {
            onEditorChipListener.selectEditorChip(EditType.NAME)
        })

        binding.textBrand.setOnClickListener(createThrottleClickListener {
            onEditorChipListener.selectEditorChip(EditType.BRAND)
        })

        binding.textExpired.setOnClickListener(createThrottleClickListener {
            onEditorChipListener.selectEditorChip(EditType.EXPIRED)
        })

        binding.textMemo.setOnClickListener(createThrottleClickListener {
            onDialogProvider.showTextInputDialog(EditType.MEMO)
        })
    }
}