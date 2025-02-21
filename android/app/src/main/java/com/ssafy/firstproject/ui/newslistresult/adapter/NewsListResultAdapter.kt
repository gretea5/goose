package com.ssafy.firstproject.ui.newslistresult.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.firstproject.R
import com.ssafy.firstproject.data.model.response.NewsAnalysisArticle
import com.ssafy.firstproject.databinding.ItemNewsBinding

private const val TAG = "NewsListResultAdapter_ssafy"
class NewsListResultAdapter(private val itemClickListener: ItemClickListener) :
    ListAdapter<NewsAnalysisArticle, NewsListResultAdapter.CustomViewHolder>(CustomComparator) {

    companion object CustomComparator : DiffUtil.ItemCallback<NewsAnalysisArticle>() {
        override fun areItemsTheSame(oldItem: NewsAnalysisArticle, newItem: NewsAnalysisArticle): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NewsAnalysisArticle, newItem: NewsAnalysisArticle): Boolean {
            return oldItem == newItem
        }
    }

    fun interface ItemClickListener {
        fun onClick(item: NewsAnalysisArticle)
    }

    inner class CustomViewHolder(private val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NewsAnalysisArticle) {
            Glide.with(binding.root)
                .load(item.topImage)
                .into(binding.ivNewsImg)
            binding.tvDate.text = item.pubDate
            binding.tvTitle.text = item.title
            binding.tvSummary.text = item.description
            binding.tvTruthPercent.text = binding.root.context.getString(R.string.reliability, item.reliability?.toInt())

            binding.root.setOnClickListener {
                itemClickListener.onClick(item)
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