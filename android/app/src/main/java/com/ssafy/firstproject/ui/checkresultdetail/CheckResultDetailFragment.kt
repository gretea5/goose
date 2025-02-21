package com.ssafy.firstproject.ui.checkresultdetail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseFragment
import com.ssafy.firstproject.data.model.NewsParagraphAnalysis
import com.ssafy.firstproject.data.model.response.NewsAnalysisArticle
import com.ssafy.firstproject.databinding.FragmentCheckResultDetailBinding
import com.ssafy.firstproject.ui.checkresultdetail.adapter.CheckResultDetailAdapter
import com.ssafy.firstproject.util.CommonUtils
import com.ssafy.firstproject.util.ViewAnimationUtil.animateProgress
import com.ssafy.firstproject.util.ViewUtil

private const val TAG = "CheckResultDetailFragment_ssafy"
class CheckResultDetailFragment : BaseFragment<FragmentCheckResultDetailBinding>(
    FragmentCheckResultDetailBinding::bind,
    R.layout.fragment_check_result_detail
) {
    private val args: CheckResultDetailFragmentArgs by navArgs()
    private lateinit var checkResultDetailAdapter: CheckResultDetailAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val newsArticle = args.newsArticle

        Log.d(TAG, "onViewCreated: $newsArticle")

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnCheckOtherNews.setOnClickListener {
            val action =
                CheckResultDetailFragmentDirections.actionDestCheckResultDetailToDestCheck()
            findNavController().navigate(action)
        }

        checkResultDetailAdapter = CheckResultDetailAdapter()

        binding.rvNewsContent.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = checkResultDetailAdapter
        }

        updateUIByData(newsArticle)

        checkResultDetailAdapter.submitList(
            combineParagraphData(
                paragraphs = newsArticle.paragraphs,
                reliabilities = newsArticle.paragraphReliabilities,
                reasons = newsArticle.paragraphReasons
            )
        )
    }

    private fun updateUIByData(newsArticle: NewsAnalysisArticle) {
        Glide.with(binding.root)
            .load(newsArticle.topImage)
            .into(binding.ivResultImage)

        binding.tvNewsTitle.text = newsArticle.title

        newsArticle.pubDate?.let {
            if (it.isNotEmpty()) binding.tvNewsDate.text = CommonUtils.convertPubDateToFormattedDate(it)
        }

        newsArticle.reliability?.let {
            val truthPercent = it.toInt()

            binding.tvDetailPercentTruth.text = getString(R.string.trust_percentage, truthPercent)

            ViewUtil.setProgressDrawableByTarget(binding.pbDetailTruth, truthPercent)
            animateProgress(binding.pbDetailTruth, truthPercent)
        }

        newsArticle.aiRate?.let {
            val aiPercent = it.toInt()

            binding.tvDetailAiWhetherPercent.text = getString(R.string.trust_percentage, aiPercent)
            ViewUtil.setProgressDrawableByTarget(binding.pbDetailAi, aiPercent)
            animateProgress(binding.pbDetailAi, aiPercent)
        }

        newsArticle.biasScore?.let {
            val biasPercent = it.toInt()

            binding.tvDetailBiasPercent.text = getString(R.string.trust_percentage, biasPercent)
            ViewUtil.setProgressDrawableByTarget(binding.pbDetailBias, biasPercent)
            animateProgress(binding.pbDetailBias, biasPercent)
        }
    }

    private fun combineParagraphData(
        paragraphs: List<String?>?,
        reliabilities: List<Double?>?,
        reasons: List<String?>?
    ): List<NewsParagraphAnalysis> {
        val safeParagraphs = paragraphs ?: emptyList()
        val safeReliabilities = reliabilities ?: emptyList()
        val safeReasons = reasons ?: emptyList()

        val size = minOf(safeParagraphs.size, safeReliabilities.size, safeReasons.size)

        val combinedList = mutableListOf<NewsParagraphAnalysis>()

        for (i in 0 until size) {
            val paragraph = safeParagraphs.getOrNull(i)
            val reliability = safeReliabilities.getOrNull(i)
            val reason = safeReasons.getOrNull(i)

            val analysis = NewsParagraphAnalysis(
                paragraph = paragraph,
                paragraphReliability = reliability,
                paragraphReason = reason
            )

            combinedList.add(analysis)
        }

        return combinedList
    }
}