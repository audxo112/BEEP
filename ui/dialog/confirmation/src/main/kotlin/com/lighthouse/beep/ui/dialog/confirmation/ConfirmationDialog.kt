package com.lighthouse.beep.ui.dialog.confirmation

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.lighthouse.beep.core.ui.exts.preventTouchPropagation
import com.lighthouse.beep.theme.R
import com.lighthouse.beep.ui.dialog.confirmation.databinding.DialogConfirmationBinding

open class ConfirmationDialog : DialogFragment() {

    companion object {
        const val TAG = "Confirmation"

        fun newInstance(params: ConfirmationParam): ConfirmationDialog {
            return ConfirmationDialog().apply {
                arguments = params.buildBundle()
            }
        }
    }

    private var _binding: DialogConfirmationBinding? = null

    private val binding: DialogConfirmationBinding
        get() = requireNotNull(_binding)

    private var onOkClickListener: OnClickListener? = null
    fun setOnOkClickListener(listener: OnClickListener?) {
        onOkClickListener = listener
    }

    private var onCancelClickListener: OnClickListener? = null
    fun setOnCancelListener(listener: OnClickListener?) {
        onCancelClickListener = listener
    }

    private var onDismissListener: OnDismissListener? = null

    fun setOnDismissListener(listener: OnDismissListener) {
        onDismissListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            val color = ConfirmationParam.getWindowBackgroundColor(context, arguments)
            window?.setBackgroundDrawable(ColorDrawable(color))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogConfirmationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpRoot()
        setUpContent()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.onDismiss()
    }

    private fun setUpRoot() {
        binding.root.setOnClickListener {
            dismiss()
        }
    }

    private fun setUpContent() {
        binding.containerContent.preventTouchPropagation()

        binding.tvMessage.apply {
            text = ConfirmationParam.getMessage(context, arguments)
            isVisible = text.isNotEmpty()
        }

        binding.btnOk.apply {
            text = ConfirmationParam.getOkText(context, arguments)
            isVisible = text.isNotEmpty()
            val okDismiss = ConfirmationParam.isOkDismiss(arguments)
            setOnClickListener { v ->
                if (onOkClickListener != null) {
                    onOkClickListener?.onClick(v)
                }
                if (okDismiss) {
                    dismiss()
                }
            }
        }

        binding.btnCancel.apply {
            text = ConfirmationParam.getCancelText(context, arguments)
            isVisible = text.isNotEmpty()
            val cancelDismiss = ConfirmationParam.isCancelDismiss(arguments)
            setOnClickListener { v ->
                if (onCancelClickListener != null) {
                    onCancelClickListener?.onClick(v)
                }
                if (cancelDismiss) {
                    dismiss()
                }
            }
        }
    }
}
