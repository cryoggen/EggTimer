package com.cryoggen.eggtimer.util

import android.text.format.DateUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.cryoggen.eggtimer.R

/**
 * Converts milliseconds to formatted mm:ss
 *
 * @param value, time in milliseconds.
 */
@BindingAdapter("elapsedTime")
fun TextView.setElapsedTime(value: Long) {
    text = if (value == 0L) {
        textSize = 32F
        resources.getString(R.string.bon_appetit)
    } else {
        val seconds = value / 1000
        if (seconds < 60) seconds.toString() else DateUtils.formatElapsedTime(seconds)
    }
}

@BindingAdapter("pressedButton")
fun Button.setTextButton(value: Boolean) {
    text = if (value) {
        resources.getString(R.string.stop_button_text)
    } else {
        resources.getString(R.string.start_button_text)
    }
}

@BindingAdapter("setImage")
fun ImageView.setImageOnStartPressed(img: Int) {
    setImageResource(img)
}

