package com.lighthouse.beep.ui.feature.gallery

import android.animation.Animator
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewPropertyAnimator
import android.view.animation.DecelerateInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.lighthouse.beep.core.common.exts.dp
import com.lighthouse.beep.core.ui.animation.SimpleAnimatorListener
import com.lighthouse.beep.core.ui.content.OnContentChangeListener
import com.lighthouse.beep.core.ui.content.registerGalleryContentObserver
import com.lighthouse.beep.core.ui.recyclerview.decoration.GridItemDecoration
import com.lighthouse.beep.core.ui.exts.createThrottleClickListener
import com.lighthouse.beep.core.ui.exts.getScrollInfo
import com.lighthouse.beep.core.ui.exts.repeatOnStarted
import com.lighthouse.beep.core.ui.recyclerview.itemtouch.OnLongPressDragListener
import com.lighthouse.beep.core.ui.utils.vibrator.VibratorGenerator
import com.lighthouse.beep.model.gallery.GalleryImage
import com.lighthouse.beep.navs.ActivityNavItem
import com.lighthouse.beep.navs.AppNavigator
import com.lighthouse.beep.navs.result.EditorResult
import com.lighthouse.beep.permission.BeepPermission
import com.lighthouse.beep.ui.feature.gallery.list.gallery.GalleryAllAdapter
import com.lighthouse.beep.ui.feature.gallery.list.gallery.GalleryRecommendAdapter
import com.lighthouse.beep.ui.feature.gallery.list.gallery.OnGalleryItemListener
import com.lighthouse.beep.ui.feature.gallery.list.selected.OnSelectedGalleryListener
import com.lighthouse.beep.ui.feature.gallery.list.selected.SelectedGalleryAdapter
import com.lighthouse.beep.ui.feature.gallery.databinding.ActivityGalleryBinding
import com.lighthouse.beep.ui.feature.gallery.list.gallery.OnGalleryAddItemListener
import com.lighthouse.beep.ui.feature.gallery.model.BucketType
import com.lighthouse.beep.ui.feature.gallery.model.DragMode
import com.lighthouse.beep.ui.feature.gallery.model.GalleryItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
internal class GalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGalleryBinding

    private val viewModel by viewModels<GalleryViewModel>()

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            refreshGalleryImage()
        }

    private val onGalleryAddListener = object: OnGalleryAddItemListener {
        override fun onClick(item: GalleryItem.AddItem) {
            galleryLauncher.launch(BeepPermission.storage)
        }
    }

    private val onGalleryListener = object : OnGalleryItemListener {
        override fun getSelectedIndexFlow(item: GalleryItem.Image): Flow<Int> {
            return viewModel.selectedList.map { list ->
                list.indexOfFirst { it.id == item.item.id }
            }.distinctUntilChanged()
        }

        override fun getAddedGifticonFlow(item: GalleryItem.Image): Flow<Boolean> {
            return viewModel.gifticonDataFlow.map {
                item.item.gifticonImageDataKey in it
            }.distinctUntilChanged()
        }

        override fun onClick(item: GalleryItem.Image) {
            viewModel.toggleItem(item.item)
        }
    }

    private val requestManager by lazy {
        Glide.with(this)
    }

    private val galleryRecommendAdapter by lazy {
        GalleryRecommendAdapter(
            requestManager,
            onGalleryAddListener,
            onGalleryListener,
        )
    }

    private val galleryAllAdapter by lazy {
        GalleryAllAdapter(
            requestManager,
            onGalleryAddListener,
            onGalleryListener,
        )
    }

    private val onSelectedGalleryListener = object : OnSelectedGalleryListener {
        override fun onClick(item: GalleryImage) {
            viewModel.deleteItem(item)
        }
    }

    private val selectedGalleryAdapter by lazy {
        SelectedGalleryAdapter(
            requestManager,
            onSelectedGalleryListener,
        )
    }

    private val recommendScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val manager = recyclerView.layoutManager as? GridLayoutManager ?: return
            val firstVisible = manager.findFirstVisibleItemPosition()
            viewModel.requestRecommend(firstVisible)
        }
    }

    private val editorLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                finish()
            } else {
                viewModel.setItems(EditorResult(it.data).list)
            }
        }

    @Inject
    lateinit var navigator: AppNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGalleryBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        setUpGalleryContentObserver()
        setUpBucketTypeTab()
        setUpSelectedGalleryList()
        setUpGalleryList()
        setUpCollectState()
        setUpOnClickEvent()
    }

    override fun onResume() {
        super.onResume()

        val oldValue = viewModel.useSelectedStorageValue
        val newValue = BeepPermission.checkSelectedStoragePermission(this)
        viewModel.setUseSelectedStorage(newValue)

        if (oldValue && !newValue) {
            viewModel.refreshGalleryImage()
        }
        if (oldValue != newValue) {
            galleryAllAdapter.refresh()
        }

        if (!BeepPermission.checkStoragePermission(this)) {
            finish()
        }
    }

    override fun onStop() {
        viewModel.cancelRecommendNext()
        binding.listGallery.stopScroll()
        saveBucketScroll()

        super.onStop()
    }

    private fun setUpGalleryContentObserver() {
        registerGalleryContentObserver(object : OnContentChangeListener {
            override fun onInsert(id: Long) {
                viewModel.insertGalleryContent(id)
            }

            override fun onDelete(id: Long) {
                viewModel.deleteGalleryContent(id)
            }
        })
    }

    private fun setUpBucketTypeTab() {
        binding.tabBucketType.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab ?: return
                binding.listGallery.stopScroll()
                saveBucketScroll()
                viewModel.setBucketType(BucketType.entries[tab.position])
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
            override fun onTabReselected(tab: TabLayout.Tab?) = Unit
        })

        BucketType.entries.forEach {
            val tab = binding.tabBucketType.newTab().apply {
                setText(it.titleRes)
            }
            binding.tabBucketType.addTab(tab)
        }
    }

    private fun saveBucketScroll() {
        val scrollInfo = binding.listGallery.getScrollInfo { position ->
            if (position > 0) 4.dp else 0
        }
        viewModel.setBucketScroll(scrollInfo = scrollInfo)
    }

    private fun setUpSelectedGalleryList() {
        binding.listSelected.adapter = selectedGalleryAdapter
        binding.listSelected.setHasFixedSize(true)
    }

    private fun setUpGalleryList() {
        binding.listGallery.layoutManager = GridLayoutManager(this, GalleryViewModel.spanCount)
        binding.listGallery.setHasFixedSize(true)
        binding.listGallery.addItemDecoration(GridItemDecoration(4f.dp))
        binding.listGallery.addOnItemTouchListener(object : OnLongPressDragListener() {
            private var dragMode = DragMode.NONE

            private fun getItem(position: Int): GalleryItem.Image? {
                return when (viewModel.bucketType.value) {
                    BucketType.ALL -> galleryAllAdapter.getItemByPosition(position)
                    BucketType.RECOMMEND -> galleryRecommendAdapter.getItemByPosition(position)
                }
            }

            override fun onStartDrag(): Boolean {
                val item = getItem(downPosition) ?: return false
                dragMode = when (viewModel.isSelectedItem(item)) {
                    true -> DragMode.DELETE
                    false -> DragMode.SELECT
                }
                viewModel.dragItem(item, dragMode)
                VibratorGenerator.vibrate(applicationContext, 100L)
                return true
            }

            override fun onMoveDrag(position: Int) {
                when {
                    movePosition in (position + 1)..downPosition -> {
                        dragToUp(movePosition - 1, position, dragMode)
                    }

                    position in (movePosition + 1)..downPosition -> {
                        dragToDown(movePosition - 1, position - 1, DragMode.DELETE)
                    }

                    downPosition in (movePosition + 1)..<position -> {
                        dragToDown(movePosition, downPosition - 1, DragMode.DELETE)
                        dragToDown(downPosition + 1, position, dragMode)
                    }

                    downPosition in (position + 1)..<movePosition -> {
                        dragToUp(movePosition, downPosition + 1, DragMode.DELETE)
                        dragToUp(downPosition - 1, position, dragMode)
                    }

                    movePosition in downPosition..<position -> {
                        dragToDown(movePosition + 1, position, dragMode)
                    }

                    position in downPosition..<movePosition -> {
                        dragToUp(movePosition + 1, position + 1, DragMode.DELETE)
                    }
                }
            }

            override fun onEndDrag() {
                dragMode = DragMode.NONE
            }

            private fun dragToDown(from: Int, to: Int, mode: DragMode) {
                for (pos in from..to) {
                    val item = getItem(pos) ?: continue
                    viewModel.dragItem(item, mode)
                }
            }

            private fun dragToUp(from: Int, to: Int, mode: DragMode) {
                for (pos in from downTo to) {
                    val item = getItem(pos) ?: continue
                    viewModel.dragItem(item, mode)
                }
            }
        })
    }

    private fun setUpCollectState() {
        repeatOnStarted {
            viewModel.bucketType.collect { type ->
                binding.listGallery.clearOnScrollListeners()
                when (type) {
                    BucketType.RECOMMEND -> {
                        binding.listGallery.adapter = galleryRecommendAdapter
                        binding.listGallery.addOnScrollListener(recommendScrollListener)
                        viewModel.requestRecommend()
                    }

                    BucketType.ALL -> {
                        binding.listGallery.adapter = galleryAllAdapter
                        viewModel.cancelRecommendNext()
                    }
                }

                val scrollInfo = viewModel.bucketScroll
                val manager = binding.listGallery.layoutManager as? GridLayoutManager
                manager?.scrollToPositionWithOffset(scrollInfo.position, scrollInfo.offset)
            }
        }

        repeatOnStarted {
            viewModel.recommendList.collect {
                galleryRecommendAdapter.submitList(it)
            }
        }

        repeatOnStarted {
            viewModel.allList.collect {
                galleryAllAdapter.submitData(it)
            }
        }

        repeatOnStarted {
            var animator: ViewPropertyAnimator? = null
            viewModel.showRecognizeProgress.collect { isShow ->
                val translationY = if (isShow) (-150f).dp else 60f.dp
                animator?.cancel()
                animator = binding.progressRecognize.animate()
                    .setInterpolator(DecelerateInterpolator())
                    .translationY(translationY)
                    .setDuration(300)
                    .setListener(object : SimpleAnimatorListener() {
                        override fun onAnimationStart(animator: Animator) {
                            binding.progressRecognize.isVisible = true
                        }

                        override fun onAnimationEnd(animator: Animator) {
                            binding.progressRecognize.isVisible = isShow
                        }
                    }).also {
                        it.start()
                    }
            }
        }

        repeatOnStarted {
            viewModel.selectedList.collect { list ->
                selectedGalleryAdapter.submitList(list)

                binding.textSelectedItemCount.text = if (list.isEmpty()) {
                    getString(R.string.selected_item_empty)
                } else {
                    getString(
                        R.string.selected_item_count,
                        list.size,
                        GalleryViewModel.maxSelectCount
                    )
                }
            }
        }

        repeatOnStarted {
            var animator: ViewPropertyAnimator? = null
            viewModel.isSelected.collect { isSelected ->
                binding.btnConfirm.isEnabled = isSelected

                val start = binding.listSelected.height
                val end = if (isSelected) resources.getDimension(R.dimen.selected_list_height)
                    .toInt() else 0.dp
                if (start != end) {
                    animator?.cancel()
                    animator = binding.listSelected.animate()
                        .setDuration(300L)
                        .setUpdateListener {
                            binding.listSelected.updateLayoutParams {
                                height = (start - (start - end) * it.animatedFraction).toInt()
                            }
                        }.setListener(object : SimpleAnimatorListener() {
                            override fun onAnimationStart(animator: Animator) {
                                binding.listSelected.adapter = null
                            }

                            override fun onAnimationEnd(animator: Animator) {
                                binding.listSelected.adapter = selectedGalleryAdapter
                            }
                        })
                    animator?.start()
                }
            }
        }
    }

    private fun setUpOnClickEvent() {
        binding.btnClose.setOnClickListener(createThrottleClickListener {
            finish()
        })

        binding.btnConfirm.setOnClickListener(createThrottleClickListener {
            if (viewModel.isSelected.value) {
                val intent =
                    navigator.getIntent(this, ActivityNavItem.Register(viewModel.selectedList.value))
                editorLauncher.launch(intent)
            }
        })
    }

    private fun refreshGalleryImage() {
        viewModel.refreshGalleryImage()
        galleryAllAdapter.refresh()
    }
}