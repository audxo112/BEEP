package com.lighthouse.beep.ui.feature.home.page.home

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.animation.DecelerateInterpolator
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.lighthouse.beep.core.common.exts.cast
import com.lighthouse.beep.core.ui.exts.preventTouchPropagation
import com.lighthouse.beep.core.ui.exts.repeatOnStarted
import com.lighthouse.beep.core.ui.exts.setOnThrottleClickListener
import com.lighthouse.beep.core.ui.exts.show
import com.lighthouse.beep.core.ui.exts.viewHeight
import com.lighthouse.beep.core.ui.exts.viewWidth
import com.lighthouse.beep.core.ui.model.ScrollInfo
import com.lighthouse.beep.model.location.DmsPos
import com.lighthouse.beep.ui.designsystem.snackbar.BeepSnackBar
import com.lighthouse.beep.ui.dialog.gifticondetail.GifticonDetailDialog
import com.lighthouse.beep.ui.dialog.gifticondetail.GifticonDetailListener
import com.lighthouse.beep.ui.dialog.gifticondetail.GifticonDetailParam
import com.lighthouse.beep.ui.feature.home.R
import com.lighthouse.beep.ui.feature.home.databinding.FragmentMainHomeBinding
import com.lighthouse.beep.ui.feature.home.model.BrandItem
import com.lighthouse.beep.ui.feature.home.model.BrandItemDiff
import com.lighthouse.beep.ui.feature.home.model.GifticonOrder
import com.lighthouse.beep.ui.feature.home.model.GifticonViewMode
import com.lighthouse.beep.ui.feature.home.model.HomeBannerItem
import com.lighthouse.beep.ui.feature.home.model.HomeItem
import com.lighthouse.beep.ui.feature.home.model.MapGifticonItem
import com.lighthouse.beep.ui.feature.home.page.home.section.HomeAdapter
import com.lighthouse.beep.ui.feature.home.page.home.section.banner.OnHomeBannerSectionListener
import com.lighthouse.beep.ui.feature.home.page.home.section.header.GifticonHeaderViewHolder
import com.lighthouse.beep.ui.feature.home.page.home.section.gifticon.OnGifticonListener
import com.lighthouse.beep.ui.feature.home.page.home.section.header.OnGifticonHeaderSectionListener
import com.lighthouse.beep.ui.feature.home.page.home.section.map.OnMapGifticonSectionListener
import com.lighthouse.beep.ui.feature.home.provider.HomeNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.math.max

@AndroidEntryPoint
class HomeMainFragment : Fragment() {

    companion object {
        const val TAG = "HomeMain"
    }

    private var _binding: FragmentMainHomeBinding? = null
    private val binding: FragmentMainHomeBinding
        get() = requireNotNull(_binding)

    private val viewModel by viewModels<HomeMainViewModel>()

    private lateinit var navigationProvider: HomeNavigation

    private val beepSnackBar by lazy {
        BeepSnackBar.Builder(requireContext())
            .setLifecycleOwner(this)
            .setRootView(binding.root)
    }

    private val requestManager: RequestManager by lazy {
        Glide.with(this)
    }

    private val homeAdapter by lazy {
        HomeAdapter(
            requestManager = requestManager,
            onHomeBannerSectionListener = onHomeBannerSectionListener,
            onMapGifticonSectionListener = onMapGifticonSectionListener,
            onGifticonHeaderSectionListener = onGifticonHeaderSectionListener,
            onGifticonListener = onGifticonListener,
        )
    }

    private val onHomeBannerSectionListener = object : OnHomeBannerSectionListener {
        override fun onClick(item: HomeBannerItem) {
        }
    }

    private val onMapGifticonSectionListener = object : OnMapGifticonSectionListener {
        override fun getMapGifticonListFlow(): Flow<List<MapGifticonItem>> {
            return flow { emit(emptyList()) }
        }

        override fun getCurrentDmsPosFlow(): Flow<DmsPos> {
            return flow { emit(DmsPos(0.0, 0.0)) }
        }

        override fun onGotoMapClick() {
        }

        override fun onClick(item: MapGifticonItem) {
        }
    }

    private val onGifticonHeaderSectionListener = object : OnGifticonHeaderSectionListener {
        override fun getBrandListFlow(): Flow<List<BrandItem>> {
            return viewModel.brandList
        }

        override fun getSelectedOrder(): Flow<GifticonOrder> {
            return viewModel.selectedOrder
        }

        override fun getViewModeFlow(): Flow<GifticonViewMode> {
            return viewModel.gifticonViewMode
        }

        override fun getBrandScrollInfo(): Flow<ScrollInfo> {
            return viewModel.brandScrollInfo
        }

        override fun onOrderClick(order: GifticonOrder) {
            viewModel.selectGifticonOrder(order)
        }

        override fun onBrandClick(item: BrandItem) {
            viewModel.selectBrand(item)
        }

        override fun onGotoEditClick() {
            viewModel.toggleGifticonViewModel()
        }

        override fun getSelectedFlow(item: BrandItem): Flow<Boolean> {
            return viewModel.selectedBrandItem
                .map { BrandItemDiff.areItemsTheSame(item, it) }
                .distinctUntilChanged()
        }

        override fun onBrandScroll(scrollInfo: ScrollInfo) {
            viewModel.setBrandScrollInfo(scrollInfo)
        }
    }

    private val onGifticonListener = object : OnGifticonListener {
        override fun getNextDayEventFlow(): Flow<Unit> {
            return viewModel.nextDayRemainingTimeFlow
        }

        override fun isSelectedFlow(item: HomeItem.GifticonItem): Flow<Boolean> {
            return viewModel.selectedGifticonListFlow
                .map { list -> list.find { it.id == item.id } != null }
                .distinctUntilChanged()
        }

        override fun getViewModeFlow(): Flow<GifticonViewMode> {
            return viewModel.gifticonViewMode
        }

        override fun onClick(item: HomeItem.GifticonItem) {
            when (viewModel.gifticonViewMode.value) {
                GifticonViewMode.VIEW -> showGifticonDetail(item)
                GifticonViewMode.EDIT -> viewModel.selectGifticon(item)
            }
        }
    }

    private fun showGifticonDetail(item: HomeItem.GifticonItem) {
        show(GifticonDetailDialog.TAG) {
            val param = GifticonDetailParam(item.id)
            GifticonDetailDialog.newInstance(param).apply {
                setGifticonDetailListener(object: GifticonDetailListener {
                    override fun onDeleteGifticon() {
                        beepSnackBar.info()
                            .setTextResId(R.string.home_delete_gifticon_message)
                            .show()
                    }

                    override fun onUseGifticon() {
                        beepSnackBar.info()
                            .setTextResId(R.string.home_use_gifticon_message)
                            .show()
                    }

                    override fun onUseCash() {
                        beepSnackBar.info()
                            .setTextResId(R.string.home_use_cash_gifticon_message)
                            .show()
                    }

                    override fun onRevertGifticon() = Unit
                })
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        navigationProvider = requireActivity().cast()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpExpiredStickyHeader()
        setUpHomeList()
        setUpCollectState()
        setUpClickEvent()
    }

    private fun setUpExpiredStickyHeader() {
        val header =
            GifticonHeaderViewHolder(binding.list, onGifticonHeaderSectionListener)
        header.bind(HomeItem.GifticonHeader)
        binding.containerStickyHeader.addView(header.itemView)
        binding.containerStickyHeader.preventTouchPropagation()
    }

    private fun setUpHomeList() {
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val button = binding.btnGotoRegister
                val offset = binding.list.computeVerticalScrollOffset()
                val current = max(button.maxWidth - offset, button.minWidth)
                if (current != button.width) {
                    val progress =
                        (current - button.minWidth).toFloat() / (button.maxWidth - button.minWidth)
                    binding.textGotoRegister.alpha = progress
                    button.updateLayoutParams { width = current }
                }

                val manager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val topChildPosition = manager.findFirstVisibleItemPosition()
                if (topChildPosition == RecyclerView.NO_POSITION) {
                    return
                }
                viewModel.setCurrentPosition(topChildPosition)
            }
        })

        binding.list.adapter = homeAdapter
        binding.btnGotoRegister.doOnPreDraw {
            binding.btnGotoRegister.maxWidth = it.viewWidth
        }
    }

    private fun setUpCollectState() {
        viewLifecycleOwner.repeatOnStarted {
            viewModel.homeList.collect {
                if (viewModel.requestStopAnimation) {
                    binding.list.itemAnimator = null
                }
                homeAdapter.submitList(it) {
                    if (viewModel.requestStopAnimation) {
                        binding.list.post {
                            binding.list.itemAnimator = DefaultItemAnimator()
                            viewModel.completeStopAnimation()
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.repeatOnStarted {
            viewModel.isShowStickyHeader.collect {
                binding.containerStickyHeader.isVisible = it
            }
        }

        viewLifecycleOwner.repeatOnStarted {
            var init = true
            viewModel.gifticonViewMode.collect { mode ->
                val registerParams = binding.btnGotoRegister.layoutParams as? MarginLayoutParams
                val endMarginRegister = registerParams?.marginEnd ?: 0
                val startRegister = binding.btnGotoRegister.translationX
                val endRegister = when (mode) {
                    GifticonViewMode.VIEW -> 0
                    GifticonViewMode.EDIT -> binding.btnGotoRegister.viewWidth + endMarginRegister
                }

                val editParams = binding.containerEdit.layoutParams as? MarginLayoutParams
                val bottomMarginEdit = editParams?.bottomMargin ?: 0
                val startEdit = binding.containerEdit.translationY
                val endEdit = when (mode) {
                    GifticonViewMode.VIEW -> 0
                    GifticonViewMode.EDIT -> -binding.containerEdit.viewHeight - bottomMarginEdit
                }

                if (!init) {
                    ValueAnimator.ofFloat(0f, 1f).apply {
                        duration = 300L
                        interpolator = DecelerateInterpolator()
                        addUpdateListener {
                            if (isAdded) {
                                binding.btnGotoRegister.translationX =
                                    startRegister - (startRegister - endRegister) * it.animatedFraction
                                binding.containerEdit.translationY =
                                    startEdit - (startEdit - endEdit) * it.animatedFraction
                            }
                        }
                    }.start()
                } else {
                    binding.btnGotoRegister.translationX = endRegister.toFloat()
                    binding.containerEdit.translationY = endEdit.toFloat()
                }
                init = false
            }
        }
    }

    private fun setUpClickEvent() {
        binding.btnGotoRegister.setOnThrottleClickListener {
            navigationProvider.gotoGallery()
        }

        binding.btnDelete.setOnThrottleClickListener {
            val selectedCount = viewModel.selectedCount
            viewModel.deleteSelectedGifticon()
            beepSnackBar.info()
                .setText(getString(R.string.home_delete_multi_gifticon_message, selectedCount))
                .show()
        }

        binding.btnUse.setOnThrottleClickListener {
            val selectedCount = viewModel.selectedCount
            viewModel.useSelectedGifticon()
            beepSnackBar.info()
                .setText(getString(R.string.home_use_multi_gifticon_message, selectedCount))
                .show()
        }
    }
}