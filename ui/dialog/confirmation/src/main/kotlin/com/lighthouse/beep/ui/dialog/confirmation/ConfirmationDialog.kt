package com.lighthouse.beep.ui.dialog.confirmation

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.lighthouse.beep.core.ui.binding.viewBindings
import com.lighthouse.beep.ui.dialog.confirmation.databinding.DialogConfirmationBinding

class ConfirmationDialog : DialogFragment(R.layout.dialog_confirmation) {

    companion object {
        fun newInstance(params: ConfirmationParam): ConfirmationDialog {
            return ConfirmationDialog().apply {
                arguments = params.buildBundle()
            }
        }
    }

    private val binding by viewBindings<DialogConfirmationBinding>()

    private var onOkClickListener: OnClickListener? = null
    fun setOnOkClickListener(listener: OnClickListener?) {
        onOkClickListener = listener
    }

    private var onCancelClickListener: OnClickListener? = null
    fun setOnCancelListener(listener: OnClickListener?) {
        onCancelClickListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpRoot()
        setUpContent()
        setUpOkButton()
        setUpCancelButton()
    }

    private fun setUpRoot() {
        binding.root.setOnClickListener {
            dismiss()
        }
    }

    private fun setUpContent() {
        binding.tvTitle.apply {
            text = ConfirmationParam.getTitle(arguments)
            isVisible = text.isNotEmpty()
        }

        binding.tvMessage.apply {
            text = ConfirmationParam.getMessage(arguments)
            isVisible = text.isNotEmpty()
        }
    }

    private fun setUpOkButton() {
        binding.tvOk.apply {
            text = ConfirmationParam.getOkText(arguments)
            isVisible = text.isNotEmpty()
            setOnClickListener { v ->
                if (onOkClickListener != null) {
                    onOkClickListener?.onClick(v)
                }
                dismiss()
            }
        }
    }

    private fun setUpCancelButton() {
        binding.tvCancel.apply {
            text = ConfirmationParam.getCancelText(arguments)
            isVisible = text.isNotEmpty()
            setOnClickListener { v ->
                if (onCancelClickListener != null) {
                    onCancelClickListener?.onClick(v)
                }
                dismiss()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        dialog?.window?.apply {
            attributes = attributes.apply {
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
        }
    }
}
