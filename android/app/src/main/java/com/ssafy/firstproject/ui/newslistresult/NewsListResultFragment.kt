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

        binding.groupNewsListResult.visibility = View.GONE
        binding.tvLoading.visibility = View.VISIBLE
        binding.lavLoadingAnimation.visibility = View.VISIBLE

        binding.lavLoadingAnimation.playAnimation()

        val newsText = args.newsText
        val newsType = args.newsType

        Log.d(TAG, "onViewCreated: $newsText")

        binding.toolbarNewsListResult.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        initAdapter()
        observeNewsArticle()

        if (viewModel.newsAnalysisArticles.value == null) {
            viewModel.getNewsArticle(KeywordSearch(text = newsText, analysisType = newsType))
        }
    }

    private fun initAdapter() {
        adapter = NewsListResultAdapter { item ->
            val action = NewsListResultFragmentDirections.actionDestNewsListResultToDestNewsResult(
                url = "",
                newsArticle = item,
                mode = getString(R.string.mode_other_analysis)
            )

            findNavController().navigate(action)
        }

        binding.rvNewsResult.adapter = adapter
    }

    private fun observeNewsArticle() {
        viewModel.newsAnalysisArticles.observe(viewLifecycleOwner) {
            binding.groupNewsListResult.visibility = View.VISIBLE
            binding.lavLoadingAnimation.visibility = View.GONE
            binding.tvLoading.visibility = View.GONE

            binding.lavLoadingAnimation.pauseAnimation()

            adapter.submitList(it)
        }
    }
}