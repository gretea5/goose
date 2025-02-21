package com.ssafy.firstproject.ui.gameresultdetail

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.ApplicationClass
import com.ssafy.firstproject.base.BaseFragment
import com.ssafy.firstproject.data.model.response.GameResultDetailResponse
import com.ssafy.firstproject.databinding.FragmentGameResultDetailBinding
import com.ssafy.firstproject.ui.gameresultdetail.viewmodel.GameResultDetailViewModel
import eightbitlab.com.blurview.RenderScriptBlur

private const val TAG = "GameResultDetailFragment"

class GameResultDetailFragment : BaseFragment<FragmentGameResultDetailBinding>(
    FragmentGameResultDetailBinding::bind,
    R.layout.fragment_game_result_detail
) {

    private val args: GameResultDetailFragmentArgs by navArgs()
    private val viewModel by viewModels<GameResultDetailViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeGameStatistics()
        viewModel.getGameResultDetail(args.newsId)
        checkLogin()
        setClickListener()
    }

    private fun setClickListener() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnNextGame.setOnClickListener {
            findNavController().navigate(R.id.action_dest_game_result_detail_to_dest_game_start)
        }

        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.dest_login)
        }
    }

    private fun observeGameStatistics() {
        viewModel.gameResultDetailResponse.observe(viewLifecycleOwner) {
            setData(it)
        }
    }

    private fun setData(data: GameResultDetailResponse) {
        binding.tvAnswer.text = data.correctAnswer
        binding.tvReason.text = data.fakeReason
        setPieChart(data.selectionPercentages)
    }

    private fun setPieChart(selections: Map<String, Double>) {
        binding.pieChart.setUsePercentValues(true)

        // 데이터 세팅
        val entries = ArrayList<PieEntry>()
        selections.entries.forEach {
            if (it.value != 0.0) {
                entries.add(PieEntry(it.value.toFloat(), it.key))
            }
        }

        // 색상 세팅
        val colorsItems = ArrayList<Int>()
        for (c in ColorTemplate.VORDIPLOM_COLORS) colorsItems.add(c)
//        for (c in ColorTemplate.JOYFUL_COLORS) colorsItems.add(c)
//        for (c in COLORFUL_COLORS) colorsItems.add(c)
//        for (c in ColorTemplate.LIBERTY_COLORS) colorsItems.add(c)
//        for (c in ColorTemplate.PASTEL_COLORS) colorsItems.add(c)
//        colorsItems.add(ColorTemplate.getHoloBlue())

        val pieDataSet = PieDataSet(entries, "")
        pieDataSet.apply {
            colors = colorsItems
            valueTextColor = Color.BLACK
            valueTextSize = 18f
        }

        val pieData = PieData(pieDataSet)
        binding.pieChart.apply {
            data = pieData
            description.isEnabled = false
            isRotationEnabled = false
            centerText = getString(R.string.answer_chart)
            setEntryLabelColor(Color.BLACK)
            setCenterTextSize(20f)
            animateY(1400, Easing.EaseInOutQuad)
            animate()
        }
    }

    private fun checkLogin() {
        // 비로그인 사용자일 경우
        if (!ApplicationClass.sharedPreferencesUtil.checkLogin()) {
            setLoginVisible()
            setBlurView()
        }
    }

    private fun setLoginVisible() {
        binding.groupLogin.visibility = View.VISIBLE
    }

    private fun setBlurView() {
        val radius = 10f
        val decorView = requireActivity().window.decorView
        val rootView = decorView.findViewById<View>(android.R.id.content) as ViewGroup
        val windowBackground = decorView.background

        binding.blurView.setupWith(rootView, RenderScriptBlur(requireContext()))
            .setFrameClearDrawable(windowBackground)
            .setBlurRadius(radius) // 블러 강도 설정
    }
}