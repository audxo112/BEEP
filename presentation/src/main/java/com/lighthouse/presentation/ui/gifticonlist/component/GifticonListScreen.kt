package com.lighthouse.presentation.ui.gifticonlist.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lighthouse.presentation.R
import com.lighthouse.presentation.ui.gifticonlist.GifticonListViewModel
import timber.log.Timber

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun GifticonListScreen(
    viewModel: GifticonListViewModel = viewModel()
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    Timber.tag("GifticonList").d("${viewState.brands}")
    viewState.selectedFilter.forEach {
        Timber.tag("Compose").d("GifticonListScreen - $it")
    }
    Timber.tag("Compose").d("--END1")
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.05f))
            .padding(horizontal = 16.dp),
        color = Color.Transparent
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(top = 24.dp)
            ) {
                BrandChipList(
                    modifier = Modifier.weight(1f),
                    brands = viewState.brands,
                    viewModel = viewModel,
                    selectedFilters = viewState.selectedFilter
                )
                IconButton(
                    modifier = Modifier,
                    onClick = {
                        viewModel.showEntireBrandsDialog()
                    }
                ) {
                    Image(
                        Icons.Outlined.Tune,
                        contentDescription = stringResource(R.string.gifticon_list_show_all_brand_chips_button)
                    )
                }
            }
            GifticonList(
                gifticons = viewState.gifticons,
                Modifier.padding(top = 64.dp)
            )
        }
        if (viewState.entireBrandsDialogShown) {
            AllBrandChipsDialog(
                brands = viewState.brands,
                modifier = Modifier
                    .padding(16.dp),
                selectedFilters = viewState.selectedFilter,
                onDismiss = {
                    viewModel.dismissEntireBrandsDialog()
                }
            )
        }
    }
}