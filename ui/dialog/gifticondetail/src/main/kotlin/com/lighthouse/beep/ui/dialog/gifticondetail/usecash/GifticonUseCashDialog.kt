package com.lighthouse.beep.ui.dialog.gifticondetail.usecash

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.lighthouse.beep.core.ui.exts.setOnThrottleClickListener
import com.lighthouse.beep.library.textformat.TextInputFormat
import com.lighthouse.beep.library.textformat.setInputFormat
import com.lighthouse.beep.theme.R
import com.lighthouse.beep.ui.dialog.gifticondetail.databinding.DialogGifticonUseCashBinding

class GifticonUseCashDialog : DialogFragment() {

    companion object {
        const val TAG = "UseCash"

        fun newInstance(param: GifticonUseCashParam): GifticonUseCashDialog {
            return GifticonUseCashDialog().apply {
                arguments = param.buildBundle()
            }
        }
    }

    private var onDismissListener: OnDismissListener? = null

    fun setOnDismissListener(listener: OnDismissListener) {
        onDismissListener = listener
    }

    private var onUseCashListener: OnUseCashListener? = null

    fun setOnUseCashListener(listener: OnUseCashListener) {
        onUseCashListener = listener
    }

    private var _binding: DialogGifticonUseCashBinding? = null
    private val binding: DialogGifticonUseCashBinding
        get() = requireNotNull(_binding)

    private val viewModel by viewModels<GifticonUseCashViewModel>()

    private val format = TextInputFormat.BALANCE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.Theme_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogGifticonUseCashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUpEditCash()
        setUpOnEventClick()
    }

    override fun onDismiss(dialog: DialogInterface) {
        onDismissListener?.onDismiss()

        super.onDismiss(dialog)
    }

    private fun setUpEditCash() {
        binding.editUseCash.setText(viewModel.displayText)
        binding.editUseCash.hint = viewModel.displayText
        binding.editUseCash.imeOptions = EditorInfo.IME_ACTION_DONE

        binding.editUseCash.setInputFormat(
            inputFormat = format,
        ) { newValue ->
            viewModel.setValue(newValue)
        }

        binding.editUseCash.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                useCash()
                true
            } else {
                false
            }
        }
        binding.editUseCash.requestFocus()
    }

    private fun setUpOnEventClick() {
        binding.btnCancel.setOnThrottleClickListener {
            dismiss()
        }

        binding.btnOk.setOnThrottleClickListener {
            useCash()
        }
    }

    private fun useCash() {
        onUseCashListener?.onUseCash(viewModel.remainCash)
        dismiss()
    }
}