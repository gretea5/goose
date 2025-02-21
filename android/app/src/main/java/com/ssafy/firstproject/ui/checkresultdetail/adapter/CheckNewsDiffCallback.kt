package com.ssafy.firstproject.ui.checkresultdetail.adapter

import androidx.recyclerview.widget.DiffUtil
import com.ssafy.firstproject.data.model.NewsParagraphAnalysis

class CheckNewsDiffCallback : DiffUtil.ItemCallback<NewsParagraphAnalysis>() {
    override fun areItemsTheSame(
        oldItem: NewsParagraphAnalysis,
        newItem: NewsParagraphAnalysis
    ): Boolean {
        return oldItem.paragraph == newItem.paragraph
    }

    override fun areContentsTheSame(
        oldItem: NewsParagraphAnalysis,
        newItem: NewsParagraphAnalysis
    ): Boolean {
        return oldItem == newItem
    }
}