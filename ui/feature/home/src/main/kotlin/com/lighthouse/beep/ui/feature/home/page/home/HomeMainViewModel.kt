package com.lighthouse.beep.ui.feature.home.page.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lighthouse.beep.auth.BeepAuth
import com.lighthouse.beep.core.common.exts.calculateNextDayRemainingTime
import com.lighthouse.beep.core.ui.model.ScrollInfo
import com.lighthouse.beep.data.repository.gifticon.GifticonRepository
import com.lighthouse.beep.ui.feature.home.R
import com.lighthouse.beep.ui.feature.home.model.BrandItem
import com.lighthouse.beep.ui.feature.home.model.GifticonOrder
import com.lighthouse.beep.ui.feature.home.model.GifticonViewMode
import com.lighthouse.beep.ui.feature.home.model.HomeBannerItem
import com.lighthouse.beep.ui.feature.home.model.HomeItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
internal class HomeMainViewModel @Inject constructor(
    private val gifticonRepository: GifticonRepository,
) : ViewModel() {

    private val _selectedGifticonOrder = MutableStateFlow(GifticonOrder.D_DAY)
    val selectedExpiredOrder = _selectedGifticonOrder.asStateFlow()

    fun setSelectGifticonOrder(order: GifticonOrder) {
        _selectedGifticonOrder.value = order
    }

    val brandList = gifticonRepository.getBrandCategoryList(BeepAuth.userUid).map { brandList ->
        listOf(BrandItem.All) + brandList.map { BrandItem.Item(it.displayBrand) }
    }

    private val _selectedBrand = MutableStateFlow<BrandItem>(BrandItem.All)
    val selectedBrand = _selectedBrand.asStateFlow()

    fun setSelectBrand(item: BrandItem) {
        _selectedBrand.value = item
    }

    private val _gifticonViewMode = MutableStateFlow(GifticonViewMode.VIEW)
    val gifticonViewMode = _gifticonViewMode.asStateFlow()

    private val selectedGifticonList = mutableListOf<HomeItem.GifticonItem>()

    private val _selectedGifticonListFlow = MutableSharedFlow<List<HomeItem.GifticonItem>>(replay = 1)
    val selectedGifticonListFlow = _selectedGifticonListFlow.asSharedFlow()

    fun selectGifticon(item: HomeItem.GifticonItem) {
        if (selectedGifticonList.contains(item)) {
            selectedGifticonList.add(item)
        } else {
            selectedGifticonList.remove(item)
        }
        viewModelScope.launch {
            _selectedGifticonListFlow.emit(selectedGifticonList)
        }
    }

    fun deleteGifticon(id: Long) {
        viewModelScope.launch {
            val gifticonIdList = listOf(id)
            gifticonRepository.deleteGifticon(BeepAuth.userUid, gifticonIdList)
        }
    }

    fun deleteSelectedGifticon() {
        viewModelScope.launch {
            val gifticonIdList = selectedGifticonList.map { it.id }
            gifticonRepository.deleteGifticon(BeepAuth.userUid, gifticonIdList)
            setGifticonViewModel(GifticonViewMode.VIEW)
        }
    }

    fun useGifticon(id: Long) {
        viewModelScope.launch {
            val gifticonIdList = listOf(id)
            gifticonRepository.useGifticonList(BeepAuth.userUid, gifticonIdList)
        }
    }


    fun useSelectedGifticon() {
        viewModelScope.launch {
            val gifticonIdList = selectedGifticonList.map { it.id }
            gifticonRepository.useGifticonList(BeepAuth.userUid, gifticonIdList)
            setGifticonViewModel(GifticonViewMode.VIEW)
        }
    }

    private fun initSelectedGifticonList() {
        selectedGifticonList.clear()
        viewModelScope.launch {
            _selectedGifticonListFlow.emit(emptyList())
        }
    }

    fun toggleGifticonViewModel() {
        val nextViewMode = when(gifticonViewMode.value) {
            GifticonViewMode.VIEW -> GifticonViewMode.EDIT
            GifticonViewMode.EDIT -> GifticonViewMode.VIEW
        }
        setGifticonViewModel(nextViewMode)
    }

    private fun setGifticonViewModel(mode: GifticonViewMode) {
        if (mode == GifticonViewMode.VIEW) {
            initSelectedGifticonList()
        }
        _gifticonViewMode.value = mode
    }

    private val gifticonList = combine(
        selectedExpiredOrder,
        selectedBrand,
    ){ order, brand ->
        order to brand
    }.flatMapLatest { (order, brand) ->
        when (brand) {
            is BrandItem.All -> gifticonRepository.getGifticonList(
                userId = BeepAuth.userUid,
                gifticonSortBy = order.sortBy,
                isAsc = false,
            )
            is BrandItem.Item -> gifticonRepository.getGifticonListByBrand(
                userId = BeepAuth.userUid,
                brand = brand.name,
                gifticonSortBy = order.sortBy,
                isAsc = false,
            )
        }
    }

    private val _brandScrollInfo = MutableStateFlow(ScrollInfo.None)
    val brandScrollInfo = _brandScrollInfo.asStateFlow()

    fun setBrandScrollInfo(info: ScrollInfo) {
        _brandScrollInfo.value = info
    }

    val nextDayRemainingTimeFlow = flow {
        while (true) {
            delay(Date().calculateNextDayRemainingTime())
            emit(Unit)
        }
    }

    private val homeBannerList = listOf(
        HomeBannerItem.BuiltIn(R.drawable.img_banner_location_coming_soon),
    )

    val homeList = gifticonList.map { gifticonList ->
        listOf(
            HomeItem.Banner(homeBannerList),
            HomeItem.GifticonHeader
        ) + gifticonList.map {
            HomeItem.GifticonItem(
                id = it.id,
                thumbnail = it.thumbnail,
                type = it.type,
                brand = it.displayBrand,
                name = it.name,
                expiredDate = it.expireAt,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val expiredHeaderIndex = homeList.map {homeList ->
        homeList.indexOfFirst {
            it is HomeItem.GifticonHeader
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, -1)

    val expiredGifticonFirstIndex = homeList.map{ homeList ->
        homeList.indexOfFirst {
            it is HomeItem.GifticonItem
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, -1)

    init {
        initSelectedGifticonList()
    }
}