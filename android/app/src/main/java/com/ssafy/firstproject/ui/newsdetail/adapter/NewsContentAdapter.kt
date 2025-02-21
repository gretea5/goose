package com.ssafy.firstproject.ui.newsdetail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.firstproject.databinding.ItemNewsContentBinding
import com.ssafy.firstproject.util.TextUtil

private const val TAG = "NewsContentAdapter"

class NewsContentAdapter :
    ListAdapter<String, NewsContentAdapter.CustomViewHolder>(CustomComparator) {

    companion object CustomComparator : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    inner class CustomViewHolder(private val binding: ItemNewsContentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: String) {
            binding.tvNewsContent.text = TextUtil.replaceSpacesWithNbsp(item)
        }   
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val binding =
            ItemNewsContentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val dto = getItem(position)
        holder.bind(dto)
    }
}