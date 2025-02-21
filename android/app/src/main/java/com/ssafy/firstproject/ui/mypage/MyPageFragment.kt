package com.ssafy.firstproject.ui.mypage

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseFragment
import com.ssafy.firstproject.databinding.FragmentMyPageBinding
import android.graphics.Color
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.ssafy.firstproject.base.ApplicationClass.Companion.sharedPreferencesUtil
import com.ssafy.firstproject.data.model.response.GameRecord
import com.ssafy.firstproject.data.model.response.UserGrowth
import com.ssafy.firstproject.data.source.remote.RetrofitUtil
import com.ssafy.firstproject.ui.mypage.viewmodel.UserGrowthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MyPageFragment"

class MyPageFragment : BaseFragment<FragmentMyPageBinding>(
    FragmentMyPageBinding::bind, R.layout.fragment_my_page
) {

    private val viewModel: UserGrowthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvMyNewsList.setOnClickListener {
            findNavController().navigate(R.id.dest_record)
        }

        val isLogin = sharedPreferencesUtil.checkLogin()
        handleLoginState(isLogin)

        binding.tvProfileEdit.setOnClickListener {
            findNavController().navigate(R.id.dest_profile_edit)
        }

        binding.tvLogout.setOnClickListener {
            findNavController().navigate(R.id.dest_logout_dialog)
        }

        observeViewModel()
        viewModel.fetchUserGrowthData()
    }

    private fun observeViewModel() {
        viewModel.userGrowthData.observe(viewLifecycleOwner) { userGrowth ->
            if (userGrowth != null) {
                updateUI(userGrowth)
                setBarChart(userGrowth.gameRecords)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                binding.barChart.setNoDataText(errorMessage)
            }
        }
    }

    private fun setBarChart(gameRecords: List<GameRecord>) {
        val barChart = binding.barChart

        // 데이터가 없는 경우 처리
        if (gameRecords.isEmpty()) {
            barChart.clear()
            barChart.setNoDataText("데이터가 없습니다.")
            return
        }

        // 1️⃣ 포함된 월 리스트 가져오기
        val months = gameRecords.map { it.month }.sorted()

        // 2️⃣ 시작 월 설정
        val startMonth = when {
            8 in months -> 8 // 8월이 포함되어 있으면 8부터 시작
            months.any { it in 9..12 } -> 9 // 9~12월이 포함되어 있으면 9부터 시작
            else -> months.first() // 그 외는 가장 작은 월부터 시작
        }

        // 3️⃣ 연속적인 월 순서로 정렬
        val sortedRecords = gameRecords.sortedBy { (it.month - startMonth + 12) % 12 }

        // 4️⃣ X축 라벨 생성
        val monthLabels = sortedRecords.map { "${it.month}월" }

        // 5️⃣ 차트 데이터 매핑
        val entries = sortedRecords.mapIndexed { index, record ->
            BarEntry(index.toFloat(), record.correctRate.toFloat()) // X축: 0부터 시작
        }

        // 6️⃣ 데이터셋 스타일 설정
        val dataSet = BarDataSet(entries, "월별 정답률").apply {
            color = Color.parseColor("#7189FF")
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "${value.toInt()}%" // 정수 값으로 표시
                }
            }
        }

        // 7️⃣ BarData 설정
        val barData = BarData(dataSet).apply {
            barWidth = 0.5f // 막대 너비 조정
        }
        barChart.data = barData

        // 8️⃣ X축 설정
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            labelCount = monthLabels.size
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index in monthLabels.indices) monthLabels[index] else ""
                }
            }
        }

        // 9️⃣ Y축 설정
        barChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 100f
            granularity = 10f
        }
        barChart.axisRight.isEnabled = false

        // 🔟 기타 차트 속성
        barChart.apply {
            description.isEnabled = false
            animateY(1000)
            setTouchEnabled(false)
            invalidate()
        }
    }


    private fun handleLoginState(isLoggedIn: Boolean) {
        if (isLoggedIn) {
            // 로그인된 경우: 마이페이지 콘텐츠 표시
            binding.clLoginRequired.visibility = View.GONE
            binding.nsv.visibility = View.VISIBLE
        } else {
            // 비로그인된 경우: "로그인이 필요합니다" 메시지 표시
            binding.clLoginRequired.visibility = View.VISIBLE
            binding.nsv.visibility = View.GONE

            // 로그인 버튼 클릭 이벤트 설정
            binding.btnLogin.setOnClickListener {
                findNavController().navigate(R.id.dest_login) // 로그인 화면으로 이동
            }

            // 회원가입 버튼 클릭 이벤트 설정
            binding.btnSignUp.setOnClickListener {
                findNavController().navigate(R.id.dest_signup) // 회원가입 화면으로 이동
            }
        }
    }


    private fun updateUI(userGrowth: UserGrowth) {

        val userName = getString(R.string.user_name, userGrowth.nickname)
        binding.tvUserName.text = userName

        // TextView 업데이트 (문제 수와 맞춘 문제 수)
        val fullText =
            getString(R.string.my_growth_text, userGrowth.totalQuestions, userGrowth.correctCount)
        val totalQuestionsPart = "${userGrowth.totalQuestions}문제"
        val correctCountPart = "${userGrowth.correctCount}문제"

// SpannableStringBuilder로 특정 부분 굵게 설정
        val spannable = SpannableStringBuilder(fullText)

// 문제 수 부분 굵게 설정
        val totalStartIndex = fullText.indexOf(totalQuestionsPart)
        if (totalStartIndex != -1) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                totalStartIndex,
                totalStartIndex + totalQuestionsPart.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

// 맞춘 문제 수 부분 굵게 설정
        val correctStartIndex = fullText.indexOf(correctCountPart)
        if (correctStartIndex != -1) {
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                correctStartIndex,
                correctStartIndex + correctCountPart.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            Log.d(TAG, "Correct count part not found: $correctCountPart") // 로그로 확인
        }

        binding.tvGrowthText.text = spannable

        // ProgressBar 업데이트
        ObjectAnimator.ofInt(binding.pbProgressBar, "progress", userGrowth.correctRate.toInt())
            .apply {
                duration = 300L
                start()
            }

        // 퍼센트 텍스트 업데이트
        binding.tvProgressPercentage.text = "${userGrowth.correctRate.toInt()}%"
    }


}
