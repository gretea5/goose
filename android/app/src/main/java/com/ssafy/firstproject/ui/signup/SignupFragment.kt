package com.ssafy.firstproject.ui.signup

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.getColor
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseFragment
import com.ssafy.firstproject.data.model.request.User
import com.ssafy.firstproject.databinding.FragmentSignupBinding
import com.ssafy.firstproject.ui.signup.viewmodel.SignupViewModel
import com.ssafy.firstproject.util.setOnSingleClickListener

private const val TAG = "SignupFragment"

class SignupFragment : BaseFragment<FragmentSignupBinding>(
    FragmentSignupBinding::bind,
    R.layout.fragment_signup
) {

    private val viewModel by viewModels<SignupViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeSignUpSuccess()
        initEvent()
    }

    private fun initEvent() {

        binding.ivSignupClose.setOnClickListener { findNavController().popBackStack() }

        binding.btnSignup.setOnSingleClickListener {
            viewModel.signUp(
                User(
                    username = binding.tieSignupIdInput.text.toString(),
                    password = binding.tieSignupPwInput.text.toString(),
                    nickname = binding.tieSignupNicknameInput.text.toString()
                )
            )
        }

        observeValidation()

        binding.tieSignupIdInput.addTextChangedListener {
            viewModel.setIdDuplicateFalse()
            viewModel.checkIdValidation(it.toString())
        }

        binding.tieSignupPwInput.addTextChangedListener {
            viewModel.checkPasswordValidation(it.toString())
        }

        binding.tieSignupPwCheckInput.addTextChangedListener {
            viewModel.checkPasswordMatch(
                binding.tieSignupPwInput.text.toString(),
                it.toString()
            )
        }

        binding.tieSignupNicknameInput.addTextChangedListener {
            viewModel.setNicknameDuplicateFalse()
            viewModel.checkNickNameValidation(it.toString())
        }

        binding.btnCheckId.setOnSingleClickListener {
            viewModel.checkIdDuplicate(binding.tieSignupIdInput.text.toString())
        }

        binding.btnCheckNickname.setOnSingleClickListener {
            viewModel.checkNicknameDuplicate(binding.tieSignupNicknameInput.text.toString())
        }
    }

    private fun observeValidation() {

        viewModel.isIdValid.observe(viewLifecycleOwner) {
            if (it) {
                binding.tvIdInputMetaInfo.visibility = View.GONE
            } else {
                binding.tvIdInputMetaInfo.visibility = View.VISIBLE
                binding.tvIdInputMetaInfo.text = getString(R.string.user_limit_message)
                binding.tvIdInputMetaInfo.setTextColor(
                    getColor(
                        requireContext(),
                        R.color.maximumRed
                    )
                )
            }

            binding.btnCheckId.isEnabled = it
            viewModel.checkAllValidation()
        }

        viewModel.isPasswordValid.observe(viewLifecycleOwner) {
            if (it) {
                binding.tvPwMetaInfo.visibility = View.GONE
                viewModel.checkPasswordMatch(
                    binding.tieSignupPwInput.text.toString(),
                    binding.tieSignupPwCheckInput.text.toString()
                )
            } else {
                binding.tvPwMetaInfo.visibility = View.VISIBLE
                binding.tvPwMetaInfo.text = getString(R.string.user_limit_message)
                binding.tvPwMetaInfo.setTextColor(getColor(requireContext(), R.color.maximumRed))
            }
            viewModel.checkAllValidation()
        }

        viewModel.isPasswordMatch.observe(viewLifecycleOwner) {
            if (it) {
                if (viewModel.isPasswordValid.value == true) {
                    binding.tvPwMetaInfo.visibility = View.VISIBLE
                    binding.tvPwMetaInfo.text = getString(R.string.user_password_message)
                    binding.tvPwMetaInfo.setTextColor(Color.BLUE)
                }
            } else {
                binding.tvPwMetaInfo.visibility = View.VISIBLE
                binding.tvPwMetaInfo.text = getString(R.string.user_not_correct_message)
                binding.tvPwMetaInfo.setTextColor(getColor(requireContext(), R.color.maximumRed))
            }
            viewModel.checkAllValidation()
        }

        viewModel.isNicknameValid.observe(viewLifecycleOwner) {
            if (it) {
                binding.tvNicknameMetaInfo.visibility = View.GONE
            } else {
                binding.tvNicknameMetaInfo.visibility = View.VISIBLE
                binding.tvNicknameMetaInfo.text = getString(R.string.nickname_limit_message)
                binding.tvNicknameMetaInfo.setTextColor(
                    getColor(
                        requireContext(),
                        R.color.maximumRed
                    )
                )
            }

            binding.btnCheckNickname.isEnabled = it
            viewModel.checkAllValidation()
        }

        viewModel.isIdDuplicate.observe(viewLifecycleOwner) {
            if (it.available) {
                binding.tvIdInputMetaInfo.visibility = View.VISIBLE
                binding.tvIdInputMetaInfo.text = it.message
                binding.tvIdInputMetaInfo.setTextColor(Color.BLUE)
            } else {
                binding.tvIdInputMetaInfo.visibility = View.VISIBLE
                binding.tvIdInputMetaInfo.text = it.message
                binding.tvIdInputMetaInfo.setTextColor(
                    getColor(
                        requireContext(),
                        R.color.maximumRed
                    )
                )
            }
            viewModel.checkAllValidation()
        }

        viewModel.isNicknameDuplicate.observe(viewLifecycleOwner) {
            if (it.available) {
                binding.tvNicknameMetaInfo.visibility = View.VISIBLE
                binding.tvNicknameMetaInfo.text = it.message
                binding.tvNicknameMetaInfo.setTextColor(Color.BLUE)
            } else {
                binding.tvNicknameMetaInfo.visibility = View.VISIBLE
                binding.tvNicknameMetaInfo.text = it.message
                binding.tvNicknameMetaInfo.setTextColor(
                    getColor(
                        requireContext(),
                        R.color.maximumRed
                    )
                )
            }
            viewModel.checkAllValidation()
        }

        viewModel.isAllValid.observe(viewLifecycleOwner) {
            binding.btnSignup.isEnabled = it
        }

    }

    private fun observeSignUpSuccess() {
        viewModel.isSignupSuccess.observe(viewLifecycleOwner) {
            if (it) {
                showToast(getString(R.string.signup_success_message))
                findNavController().navigate(R.id.action_dest_signup_to_dest_login)
            } else {
                showToast(getString(R.string.signup_fail_message))
            }
        }
    }
}