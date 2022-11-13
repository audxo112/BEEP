package com.lighthouse.presentation.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lighthouse.domain.model.CustomError
import com.lighthouse.domain.usecase.GetBrandPlaceInfosUseCase
import com.lighthouse.presentation.mapper.toPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getBrandPlaceInfosUseCase: GetBrandPlaceInfosUseCase
) : ViewModel() {

    private val brandList = arrayListOf("스타벅스", "베스킨라빈스", "BHC", "BBQ")

    private val _state: MutableStateFlow<MapState> = MutableStateFlow(MapState.Loading)
    val state: StateFlow<MapState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            for (query in brandList) {
                getBrandPlaceInfosUseCase(query, "37.2840", "127.1071", "500", 5)
                    .onSuccess { brandPlaceInfos ->
                        _state.value = MapState.Success(brandPlaceInfos.map { it.toPresentation() })
                    }
                    .onFailure { throwable ->
                        when (throwable) {
                            CustomError.NetworkFailure -> _state.value = MapState.NetworkFailure
                            CustomError.NotFoundBrandPlaceInfos -> _state.value = MapState.NotFoundSearchResults
                            else -> _state.value = MapState.Failure
                        }
                    }
            }
        }
    }
}
