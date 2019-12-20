package co.netguru.baby.monitor.client.common

import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import co.netguru.baby.monitor.client.R

class YesNoDialog : androidx.fragment.app.DialogFragment() {

    private val title by lazy { requireArguments().getInt(KEY_TITLE) }
    private val message by lazy { requireArguments().getString(KEY_MESSAGE) }
    private val positiveButton by lazy { requireArguments().getInt(KEY_POSITIVE) }
    private val requestCode by lazy { requireArguments().getInt(KEY_REQUEST_CODE) }

    private val yesNoDialogClickListener
        get() = parentFragment as? YesNoDialogClickListener
            ?: activity as YesNoDialogClickListener

    override fun onCreateDialog(savedInstanceState: Bundle?): AlertDialog =
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButton) { _, _ ->
                yesNoDialogClickListener.onYesClick(requestCode, requireArguments())
            }
            .setNegativeButton(R.string.dialog_no) { _, _ ->
                yesNoDialogClickListener.onDismiss(requestCode)
            }
            .create()

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        yesNoDialogClickListener.onDismiss(requestCode)
    }

    interface YesNoDialogClickListener {
        fun onYesClick(requestCode: Int, params: Bundle)
        fun onDismiss(requestCode: Int) = Unit
    }

    companion object {
        private const val KEY_TITLE = "key:title"
        private const val KEY_MESSAGE = "key:message"
        private const val KEY_POSITIVE = "key:positive"
        private const val KEY_REQUEST_CODE = "key:request_code"

        fun newInstance(
            @StringRes title: Int,
            message: String,
            @StringRes positiveButton: Int = R.string.dialog_yes,
            requestCode: Int = -1,
            params: Bundle? = null
        ) = YesNoDialog().apply {
            arguments = bundleOf(
                KEY_TITLE to title,
                KEY_MESSAGE to message,
                KEY_POSITIVE to positiveButton,
                KEY_REQUEST_CODE to requestCode
            ).also {
                if (params != null) {
                    it.putAll(params)
                }
            }
        }
    }
}
