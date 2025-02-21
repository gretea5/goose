package com.ssafy.firstproject.ui.home

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseFragment
import com.ssafy.firstproject.databinding.FragmentHomeBinding
import com.ssafy.firstproject.ui.home.adapter.NewsAdapter
import com.ssafy.firstproject.ui.home.viewmodel.HomeViewModel

private const val TAG = "HomeFragment"

class HomeFragment : BaseFragment<FragmentHomeBinding>(
    FragmentHomeBinding::bind,
    R.layout.fragment_home
) {
    private lateinit var adapter: NewsAdapter
    private val viewModel by viewModels<HomeViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdapter()
        observeNewsList()

        binding.tvSearch.setOnClickListener {
            findNavController().navigate(R.id.dest_search)
        }
    }

    private fun initAdapter() {
        adapter = NewsAdapter { id ->
            val action = HomeFragmentDirections.actionDestHomeToDestNewsDetail(id)
            findNavController().navigate(action)
        }
        binding.rvNews.adapter = adapter
    }

    private fun observeNewsList() {
        viewModel.newsList.observe(viewLifecycleOwner) {
            binding.lavLoadingAnimation.visibility = View.GONE
            binding.lavLoadingAnimation.pauseAnimation()
            adapter.submitList(it)
        }
    }
}