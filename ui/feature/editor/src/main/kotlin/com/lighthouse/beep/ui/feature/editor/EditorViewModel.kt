package com.lighthouse.beep.ui.feature.editor

import android.graphics.Bitmap
import android.graphics.RectF
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lighthouse.beep.core.common.exts.EMPTY_DATE
import com.lighthouse.beep.domain.usecase.recognize.RecognizeBalanceUseCase
import com.lighthouse.beep.domain.usecase.recognize.RecognizeBarcodeUseCase
import com.lighthouse.beep.domain.usecase.recognize.RecognizeExpiredUseCase
import com.lighthouse.beep.domain.usecase.recognize.RecognizeGifticonUseCase
import com.lighthouse.beep.domain.usecase.recognize.RecognizeTextUseCase
import com.lighthouse.beep.model.gallery.GalleryImage
import com.lighthouse.beep.ui.feature.editor.model.EditData
import com.lighthouse.beep.ui.feature.editor.model.EditorChip
import com.lighthouse.beep.ui.feature.editor.model.GifticonData
import com.lighthouse.beep.ui.feature.editor.model.EditType
import com.lighthouse.beep.ui.feature.editor.model.EditorPage
import com.lighthouse.beep.ui.feature.editor.model.toGifticonData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recognizeGifticonUseCase: RecognizeGifticonUseCase,
    private val recognizeTextUseCase: RecognizeTextUseCase,
    private val recognizeBarcodeUseCase: RecognizeBarcodeUseCase,
    private val recognizeBalanceUseCase: RecognizeBalanceUseCase,
    private val recognizeExpiredUseCase: RecognizeExpiredUseCase,
) : ViewModel() {

    private val _galleryImage = MutableStateFlow(EditorParam.getGalleryList(savedStateHandle))
    val galleryImage = _galleryImage.asStateFlow()

    fun deleteItem(item: GalleryImage) {
        _galleryImage.value = _galleryImage.value.filter { it.id != item.id }
    }

    private val _selectedGifticon = MutableStateFlow(_galleryImage.value.firstOrNull())
    val selectedGifticon = _selectedGifticon.asStateFlow()

    fun selectGifticon(item: GalleryImage) {
        _selectedGifticon.value = item
    }

    private val _selectedEditorChip = MutableStateFlow<EditorChip>(EditorChip.Preview)
    val selectedEditorChip = _selectedEditorChip.asStateFlow()

    val selectedEditType: EditType?
        get() {
            val chip = selectedEditorChip.value as? EditorChip.Property
            return chip?.type
        }

    val selectedEditorPage = selectedEditorChip.map { chip ->
        when (chip) {
            is EditorChip.Preview -> EditorPage.PREVIEW
            else -> EditorPage.CROP
        }
    }.distinctUntilChanged()

    fun selectEditorChip(item: EditorChip) {
        _selectedEditorChip.value = item
    }

    private val gifticonDataMap = mutableMapOf<Long, GifticonData>()

    fun getGifticonData(id: Long): GifticonData? {
        return gifticonDataMap[id]
    }

    private val _gifticonDataMapFlow = MutableSharedFlow<Map<Long, GifticonData>>(replay = 1)
    val gifticonDataMapFlow = _gifticonDataMapFlow.asSharedFlow()

    val isRegisterActivated = gifticonDataMapFlow
        .map { map -> map.values.any { !it.isInvalid } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun updateGifticonData(editData: EditData) {
        val selectedItem = selectedGifticon.value ?: return
        val data = gifticonDataMap[selectedItem.id] ?: return
        if (!editData.isModified(data)) {
            return
        }
        gifticonDataMap[selectedItem.id] = editData.updatedGifticon(data)
        viewModelScope.launch {
            _gifticonDataMapFlow.emit(gifticonDataMap)
        }
    }

    suspend fun updateGifticonData(
        type: EditType,
        bitmap: Bitmap,
        rect: RectF,
    ) {
        val editData = when (type) {
            EditType.NAME,
            EditType.BRAND -> {
                val result = recognizeTextUseCase(bitmap).getOrDefault("")
                type.createEditDataWithCrop(result, rect)
            }
            EditType.BARCODE -> {
                val barcode = recognizeBarcodeUseCase(bitmap).getOrDefault("")
                EditData.CropBarcode(barcode, rect)
            }
            EditType.BALANCE -> {
                val balance = recognizeBalanceUseCase(bitmap).getOrDefault(0).toString()
                EditData.CropBalance(balance, rect)
            }
            EditType.EXPIRED -> {
                val date = recognizeExpiredUseCase(bitmap).getOrDefault(EMPTY_DATE)
                EditData.CropExpired(date, rect)
            }
            else -> EditData.None
        }
        if(editData != EditData.None) {
            updateGifticonData(editData)
        }
    }

    val selectedGifticonData = combine(
        selectedGifticon,
        gifticonDataMapFlow,
    ) { selected, gifticonMap ->
        selected ?: return@combine null
        gifticonMap[selected.id]
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val selectedGifticonDataFlow = selectedGifticonData.filterNotNull()

    val editorPropertyChipList = selectedGifticonDataFlow.map { data ->
        EditType.entries.filter { type ->
            when (type) {
                EditType.MEMO -> false
                EditType.BALANCE -> data.isCashCard
                else -> true
            }
        }.map {
            EditorChip.Property(it)
        }
    }.distinctUntilChanged()

    private val _recognizeLoading = MutableStateFlow(false)
    val recognizeLoading = _recognizeLoading.asStateFlow()

    init {
        _recognizeLoading.value = true
        viewModelScope.launch {
            _gifticonDataMapFlow.emit(emptyMap())
            galleryImage.value.map { gallery ->
                launch {
                    val data = recognizeGifticonUseCase(gallery).getOrNull()
                        .toGifticonData(gallery.contentUri)
                    gifticonDataMap[gallery.id] = data
                }
            }.joinAll()
            _gifticonDataMapFlow.emit(gifticonDataMap)
        }.also {
            it.invokeOnCompletion {
                _recognizeLoading.value = false
            }
        }
    }
}