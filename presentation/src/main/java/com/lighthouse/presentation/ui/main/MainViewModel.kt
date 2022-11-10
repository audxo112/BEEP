package com.lighthouse.presentation.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lighthouse.presentation.R
import com.lighthouse.presentation.ui.main.event.MainDirections
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val directionsFlow = MutableSharedFlow<MainDirections>()

    val selectedMenuItem = MutableStateFlow(R.id.menu_home)

    fun gotoAddGifticon() {
        viewModelScope.launch {
            directionsFlow.emit(MainDirections.ADD_GIFTICON)
        }
    }

    fun gotoMenuItem(itemId: Int): Boolean {
        if (selectedMenuItem.value == itemId) {
            return true
        }
        selectedMenuItem.value = itemId
        viewModelScope.launch {
            val directions = when (itemId) {
                R.id.menu_list -> MainDirections.LIST
                R.id.menu_home -> MainDirections.HOME
                R.id.menu_setting -> MainDirections.SETTING
                else -> null
            } ?: return@launch
            directionsFlow.emit(directions)
        }
        return true
    }
}
