package com.ssafy.firstproject.ui.game

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ssafy.firstproject.R
import com.ssafy.firstproject.data.model.request.UserChoiceRequest
import com.ssafy.firstproject.databinding.DialogChoiceBinding
import com.ssafy.firstproject.ui.game.viewmodel.ChoiceDialogViewModel
import com.ssafy.firstproject.util.setOnSingleClickListener

class ChoiceDialogFragment : BottomSheetDialogFragment() {

    private var _binding: DialogChoiceBinding? = null
    private val binding get() = _binding!!
    private val args: ChoiceDialogFragmentArgs by navArgs()
    private val viewModel by viewModels<ChoiceDialogViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogChoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeSubmitGameResultSuccess()

        with(binding) {
            btnFakeNews.setOnSingleClickListener { submitGameResult(getString(R.string.fake_news)) }
            btnExaggeratedNews.setOnSingleClickListener { submitGameResult(getString(R.string.exaggerated_news)) }
            btnClickbait.setOnSingleClickListener { submitGameResult(getString(R.string.clickbait)) }
        }
    }

    private fun submitGameResult(choice: String) {
        viewModel.submitUserChoice(
            UserChoiceRequest(
                dwellTime = args.totalTimeSpent,
                newsId = args.newsId,
                userChoice = choice
            )
        )
    }

    private fun observeSubmitGameResultSuccess() {
        viewModel.userChoiceResult.observe(viewLifecycleOwner) {
            val action = ChoiceDialogFragmentDirections.actionDestChoiceDialogToDestGameResult(it)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}