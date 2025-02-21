package com.ssafy.firstproject.ui.gameresult

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseFragment
import com.ssafy.firstproject.data.model.response.GameResultResponse
import com.ssafy.firstproject.data.model.response.TimeRanking
import com.ssafy.firstproject.databinding.FragmentGameResultBinding
import com.ssafy.firstproject.ui.gameresult.viewmodel.GameResultViewModel

private const val TAG = "GameResultFragment"

class GameResultFragment : BaseFragment<FragmentGameResultBinding>(
    FragmentGameResultBinding::bind,
    R.layout.fragment_game_result
) {

    private val args: GameResultFragmentArgs by navArgs()
    private val viewModel by viewModels<GameResultViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnResultDetail.setOnClickListener {
            val action =
                GameResultFragmentDirections.actionDestGameResultToDestGameResultDetail(args.choiceResult.newsId)
            findNavController().navigate(action)
        }

        observeGameResult()

        viewModel.getGameResult(args.choiceResult.newsId)
    }

    private fun observeGameResult() {
        viewModel.gameResult.observe(viewLifecycleOwner) {
            setData(it)
        }
    }

    private fun setData(result: GameResultResponse) {
        setAnswerResult()
        setNickname(result.nickname)
        setAnswerDetails()
        setRanking(result.ranking)
    }

    private fun setAnswerResult() {
        val (imageRes, iconRes, descriptionRes) = if (args.choiceResult.correct) {
            Triple(R.drawable.ic_grinning_face, R.drawable.ic_check, R.string.result_correct_answer)
        } else {
            Triple(
                R.drawable.ic_weary_face,
                R.drawable.ic_error_close,
                R.string.result_wrong_answer
            )
        }
        Glide.with(binding.root).load(imageRes).into(binding.ivAnswer)
        Glide.with(binding.root).load(iconRes).into(binding.ivAnswerResult)
        binding.tvAnswerResultDescription.text = getString(descriptionRes)
    }

    private fun setNickname(nickname: String) {
        binding.tvNickname.text = if (nickname == "guest") "???" else nickname
    }

    private fun setAnswerDetails() {
        binding.tvAnswerResult.text =
            getString(R.string.result_answer, args.choiceResult.correctAnswer)
        binding.tvMyAnswer.text = getString(R.string.my_answer, args.choiceResult.userChoice)
        binding.tvTime.text =
            getString(R.string.total_time_spent, millisecondsToSeconds(args.choiceResult.dwellTime))
    }

    private fun setRanking(ranking: List<TimeRanking>) {
        binding.tvNoRanker.visibility = if (ranking.isEmpty()) View.VISIBLE else View.GONE

        val rankViews = listOf(
            Triple(binding.groupGold, binding.tvTimeGold, binding.tvNicknameGold),
            Triple(binding.groupSilver, binding.tvTimeSilver, binding.tvNicknameSilver),
            Triple(binding.groupBronze, binding.tvTimeBronze, binding.tvNicknameBronze)
        )

        rankViews.forEachIndexed { index, (group, timeView, nicknameView) ->
            if (index < ranking.size) {
                group.visibility = View.VISIBLE
                timeView.text = getString(
                    R.string.total_time_spent,
                    millisecondsToSeconds(ranking[index].dwellTime)
                )
                nicknameView.text = ranking[index].nickname
            } else {
                group.visibility = View.GONE
            }
        }
    }

    private fun millisecondsToSeconds(milliseconds: Long): Double {
        return milliseconds / 1000.0
    }
}