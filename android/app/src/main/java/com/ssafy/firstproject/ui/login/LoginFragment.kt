package com.ssafy.firstproject.ui.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseFragment
import com.ssafy.firstproject.data.model.request.User
import com.ssafy.firstproject.databinding.FragmentLoginBinding
import com.ssafy.firstproject.ui.login.viewmodel.LoginViewModel
import com.ssafy.firstproject.util.setOnSingleClickListener

class LoginFragment : BaseFragment<FragmentLoginBinding>(
    FragmentLoginBinding::bind,
    R.layout.fragment_login
) {

    val viewModel by viewModels<LoginViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvSignUp.setOnClickListener {
            findNavController().navigate(R.id.dest_signup)
        }

        binding.btnLogin.setOnSingleClickListener {
            if (
                binding.tieLoginIdInput.text.toString().isEmpty()
                || binding.tieLoginPwInput.text.toString().isEmpty()
            ) {
                showToast(getString(R.string.login_limit_message))
                return@setOnSingleClickListener
            }
            viewModel.login(
                User(
                    username = binding.tieLoginIdInput.text.toString(),
                    password = binding.tieLoginPwInput.text.toString()
                )
            )
        }

        observeLogin()
    }

    private fun observeLogin() {
        viewModel.isLoginSuccess.observe(viewLifecycleOwner) {
            if (it) {
                showToast(getString(R.string.login_success_message))
                findNavController().navigate(
                    R.id.dest_home,  // 이동할 목적지
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true) // 네비게이션 그래프의 루트까지 제거
                        .build()
                )
            } else {
                showToast(getString(R.string.login_fail_message))
            }
        }
    }

}