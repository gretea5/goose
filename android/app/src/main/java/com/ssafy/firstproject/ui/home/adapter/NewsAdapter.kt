package com.ssafy.firstproject.ui.home.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.firstproject.R
import com.ssafy.firstproject.data.model.response.NewsArticle
import com.ssafy.firstproject.databinding.ItemNewsBinding
import com.ssafy.firstproject.util.CommonUtils

private const val TAG = "NewsAdapter"

class NewsAdapter(private val itemClickListener: ItemClickListener) :
    ListAdapter<NewsArticle, NewsAdapter.CustomViewHolder>(CustomComparator) {

    companion object CustomComparator : DiffUtil.ItemCallback<NewsArticle>() {
        override fun areItemsTheSame(oldItem: NewsArticle, newItem: NewsArticle): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NewsArticle, newItem: NewsArticle): Boolean {
            return oldItem == newItem
        }
    }

    fun interface ItemClickListener {
        fun onClick(id: String)
    }

    inner class CustomViewHolder(private val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NewsArticle) {
            Glide.with(binding.root)
                .load(item.topImage)
                .into(binding.ivNewsImg)
            binding.tvDate.text = CommonUtils.formatDateYYMMDD(item.pubDateTimestamp)
            binding.tvTitle.text = item.title
            binding.tvSummary.text = item.description
            binding.tvTruthPercent.text =
                binding.root.context.getString(R.string.reliability, item.reliability.toInt())

            binding.root.setOnClickListener {
                itemClickListener.onClick(item.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding =
            ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val dto = getItem(position)
        holder.bind(dto)
    }

}