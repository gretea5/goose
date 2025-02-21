package com.ssafy.firstproject.ui.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.ApplicationClass.Companion.sharedPreferencesUtil
import com.ssafy.firstproject.databinding.DialogLogoutBinding
import com.ssafy.firstproject.ui.mypage.viewmodel.LogoutDialogViewModel
import com.ssafy.firstproject.util.setOnSingleClickListener

class LogoutDialogFragment : DialogFragment() {

    private var _binding: DialogLogoutBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LogoutDialogViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogLogoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDialogSize()

        binding.btnNo.setOnClickListener {
            dismiss()
        }

        binding.btnYes.setOnSingleClickListener {
            viewModel.logout()
        }

        observeLogoutSuccess()
    }

    private fun setDialogSize() {
        val displayMetrics = resources.displayMetrics
        val widthPixels = displayMetrics.widthPixels

        val params = dialog?.window?.attributes
        params?.width = (widthPixels * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun observeLogoutSuccess() {
        viewModel.isLogoutSuccess.observe(viewLifecycleOwner) {
            if (it) {
                findNavController().navigate(
                    R.id.dest_home,  // 이동할 목적지
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true) // 네비게이션 그래프의 루트까지 제거
                        .build()
                )

                Toast.makeText(
                    requireContext(),
                    getString(R.string.logout_success_message),
                    Toast.LENGTH_SHORT
                ).show()

                sharedPreferencesUtil.removeAccessToken()
                sharedPreferencesUtil.removeRefreshToken()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}