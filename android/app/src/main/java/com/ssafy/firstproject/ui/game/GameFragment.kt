package com.ssafy.firstproject.ui.game

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseFragment
import com.ssafy.firstproject.databinding.FragmentGameBinding
import com.ssafy.firstproject.ui.game.viewmodel.GameViewModel

private const val TAG = "GameFragment"

class GameFragment : BaseFragment<FragmentGameBinding>(
    FragmentGameBinding::bind,
    R.layout.fragment_game
) {

    private val viewModel by viewModels<GameViewModel>()
    private var startTime: Long = 0
    private var totalTimeSpent: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.fab.setOnClickListener {
            calcTotalTimeSpent()
            viewModel.fakeNews.value?.let {
                val action =
                    GameFragmentDirections.actionDestGameToDestChoiceDialog(
                        newsId = it.id,
                        totalTimeSpent = totalTimeSpent
                    )
                findNavController().navigate(action)
            }
        }

        observeFakeNews()
    }

    override fun onResume() {
        super.onResume()
        startTime = System.currentTimeMillis()
    }

    override fun onPause() {
        super.onPause()
        calcTotalTimeSpent()
    }

    private fun calcTotalTimeSpent() {
        val endTime = System.currentTimeMillis()
        val timeSpent = endTime - startTime
        totalTimeSpent += timeSpent

        Log.d(TAG, "현재 화면 체류 시간: ${timeSpent}ms, 누적 시간: ${totalTimeSpent}ms")
    }

    private fun observeFakeNews() {
        viewModel.fakeNews.observe(viewLifecycleOwner) {
            Glide.with(binding.root)
                .load(it.imageUrl)
                .thumbnail(
                    Glide.with(this).load(R.drawable.ic_loading)
                )
                .into(binding.iv)
            binding.tvTitle.text = it.title
            binding.tvDate.text = it.newsDate
            binding.tvBody.text = it.content
        }
    }
}