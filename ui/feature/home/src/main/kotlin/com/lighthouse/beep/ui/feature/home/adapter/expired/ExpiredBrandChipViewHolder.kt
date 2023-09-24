package com.lighthouse.beep.ui.feature.home.adapter.expired

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.lighthouse.beep.core.ui.exts.setOnThrottleClickListener
import com.lighthouse.beep.core.ui.viewholder.LifecycleViewHolder
import com.lighthouse.beep.ui.feature.home.R
import com.lighthouse.beep.ui.feature.home.databinding.ItemExpiredBrandChipBinding
import com.lighthouse.beep.ui.feature.home.model.ExpiredBrandItem

class ExpiredBrandChipViewHolder(
    parent: ViewGroup,
    private val listener: OnExpiredBrandListener,
    private val binding: ItemExpiredBrandChipBinding = ItemExpiredBrandChipBinding.inflate(
        LayoutInflater.from(parent.context)
    ),
) : LifecycleViewHolder<ExpiredBrandItem>(binding.root) {

    override fun bind(item: ExpiredBrandItem) {
        super.bind(item)

        binding.chip.text = when(item) {
            is ExpiredBrandItem.All -> context.getString(R.string.brand_all)
            is ExpiredBrandItem.Item -> item.name
        }
    }

    override fun onSetUpClickEvent(item: ExpiredBrandItem) {
        binding.chip.setOnThrottleClickListener {
            listener.onClick(item)
        }
    }

    override fun onCollectState(lifecycleOwner: LifecycleOwner, item: ExpiredBrandItem) {
        listener.getSelectedFlow().collect(lifecycleOwner) { selected ->
            binding.chip.isSelected = item == selected
        }
    }
}