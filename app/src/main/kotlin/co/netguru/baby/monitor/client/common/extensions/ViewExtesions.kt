package co.netguru.baby.monitor.client.common.extensions

import android.support.design.widget.Snackbar
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText

fun View.setVisible(boolean: Boolean) {
    this.visibility = if (boolean) {
        View.VISIBLE
    } else {
        View.GONE
    }
}

fun View.showSnackbar(text: String, duration: Int) {
    Snackbar.make(this, text, duration).show()
}

fun EditText.afterTextChanged(afterChange: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterChange(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
    })
}

val EditText.trimmedText: String
    get() = this.text.toString().trim()
