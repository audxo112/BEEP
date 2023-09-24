package com.lighthouse.beep.core.ui.exts

import android.view.View
import com.lighthouse.beep.core.ui.utils.throttle.OnThrottleClickListener

fun View.setOnThrottleClickListener(
    throttleTime: Long = OnThrottleClickListener.DEFAULT_THROTTLE_TIME,
    listener: () -> Unit,
) {
    setOnClickListener(OnThrottleClickListener(throttleTime, listener))
}