package com.ssafy.firstproject.ui.newslistresult

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseFragment
import com.ssafy.firstproject.data.model.request.KeywordSearch
import com.ssafy.firstproject.databinding.FragmentNewsListResultBinding
import com.ssafy.firstproject.ui.newslistresult.adapter.NewsListResultAdapter
import com.ssafy.firstproject.ui.newslistresult.viewmodel.NewsListResultViewModel

private const val TAG = "NewsListResultFragment"

class NewsListResultFragment : BaseFragment<FragmentNewsListResultBinding>(
    FragmentNewsListResultBinding::bind,
    R.layout.fragment_news_list_result
) {
    private val args: NewsListResultFragmentArgs by navArgs()
    private val viewModel by viewModels<NewsListResultViewModel>()

    private lateinit var adapter: NewsListResultAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsText = args.newsText

        Log.d(TAG, "onViewCreated: $newsText")

        binding.toolbarNewsListResult.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        initAdapter()
        observeNewsArticle()

        viewModel.getNewsArticle(KeywordSearch(text = newsText))
    }

    private fun initAdapter() {
        adapter = NewsListResultAdapter { item ->
            Log.d(TAG, "initAdapter: $item")
        }

        binding.rvNewsResult.adapter = adapter
    }

    private fun observeNewsArticle() {
        viewModel.newsAnalysisArticles.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }
}