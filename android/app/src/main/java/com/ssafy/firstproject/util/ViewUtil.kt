package com.ssafy.firstproject.util

import android.util.TypedValue
import android.view.View
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import com.ssafy.firstproject.R

object ViewUtil {
    fun setProgressDrawableByTarget(progressBar: ProgressBar, targetProgress: Int) {
        val context = progressBar.context
        val drawableRes = when {
            targetProgress < 33 -> R.drawable.bg_progress_bar_33
            targetProgress < 66 -> R.drawable.bg_progress_bar_66
            else -> R.drawable.bg_progress_bar_100
        }
        progressBar.progressDrawable = ContextCompat.getDrawable(context, drawableRes)
    }

    // dp를 px로 변환하는 확장 함수
    fun Int.toPx(view: View): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            view.resources.displayMetrics
        ).toInt()
    }

    fun setConstraintLayoutBackgroundByTarget(constraintLayout: ConstraintLayout, targetProgress: Int) {
        val context = constraintLayout.context

        val backgroundRes = R.drawable.bg_corn_flower_blue_8dp

        constraintLayout.background = ContextCompat.getDrawable(context, backgroundRes)

        val paddingPx = 12.toPx(constraintLayout)
        constraintLayout.updatePadding(paddingPx, paddingPx, paddingPx, paddingPx)
    }

}