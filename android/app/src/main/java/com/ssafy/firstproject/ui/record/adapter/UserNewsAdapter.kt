package com.ssafy.firstproject.ui.record.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ssafy.firstproject.R
import com.ssafy.firstproject.data.mapper.NewsMapper
import com.ssafy.firstproject.data.model.response.NewsAnalysisArticle
import com.ssafy.firstproject.data.model.response.UserNews
import com.ssafy.firstproject.databinding.ItemNewsBinding
import com.ssafy.firstproject.util.DateUtil

class UserNewsAdapter(private val itemClickListener: ItemClickListener) :
    ListAdapter<UserNews, UserNewsAdapter.UserNewsViewHolder>(UserNewsDiffCallback) {

    companion object UserNewsDiffCallback : DiffUtil.ItemCallback<UserNews>() {
        override fun areItemsTheSame(oldItem: UserNews, newItem: UserNews): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserNews, newItem: UserNews): Boolean {
            return oldItem == newItem
        }
    }

    fun interface ItemClickListener {
        fun onClick(newsAnalysisArticle: NewsAnalysisArticle)
    }

    inner class UserNewsViewHolder(private val binding: ItemNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserNews) {
            Glide.with(binding.root)
                .load(item.topImage)
                .error(R.drawable.ic_goose)
                .into(binding.ivNewsImg)

            binding.tvTitle.text = item.title
            binding.tvSummary.text = item.description
            binding.tvTruthPercent.text = binding.root.context.getString(
                R.string.reliability,
                item.reliability.toInt()
            )

            binding.tvDate.text = "${DateUtil.parseDate(item.analysisRequestedAt)} ${item.analysisType} 판별"

            val newsAnalysisArticle = NewsMapper.mapToNewsAnalysisArticle(item)

            binding.root.setOnClickListener {
                itemClickListener.onClick(newsAnalysisArticle)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserNewsViewHolder {
        val binding =
            ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserNewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserNewsViewHolder, position: Int) {
        val dto = getItem(position)
        holder.bind(dto)
    }
}
