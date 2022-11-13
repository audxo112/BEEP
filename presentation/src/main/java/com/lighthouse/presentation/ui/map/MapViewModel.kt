package com.lighthouse.presentation.ui.map

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lighthouse.domain.usecase.GetBrandPlaceInfosUseCase
import com.lighthouse.presentation.mapper.toPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getBrandPlaceInfosUseCase: GetBrandPlaceInfosUseCase
) : ViewModel() {

    private val brandList = arrayListOf("스타벅스", "베스킨라빈스", "BHC", "BBQ")

    init {
        viewModelScope.launch {
            for (query in brandList) {
                getBrandPlaceInfosUseCase(query, "37.2840", "127.1071", "500", 5)
                    .onSuccess { brandPlaceInfos ->
                        val brandPlaceInfoUiModels = brandPlaceInfos.map { it.toPresentation() }
                        Log.d("TAG", "브랜드 리스트 갖고 오기 성공 -> $brandPlaceInfoUiModels")
                    }
                    .onFailure { Log.d("TAG", " 브랜드 리스트 갖고 오기 실패 -> $it") }
            }
        }
    }
}
