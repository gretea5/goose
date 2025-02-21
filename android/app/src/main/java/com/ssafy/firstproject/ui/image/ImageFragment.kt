package com.ssafy.firstproject.ui.image

import android.os.Bundle
import android.view.View
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseFragment
import androidx.navigation.fragment.findNavController
import com.ssafy.firstproject.databinding.FragmentImageBinding

private const val TAG = "ImageFragment"

class ImageFragment : BaseFragment<FragmentImageBinding>(
    FragmentImageBinding::bind,
    R.layout.fragment_image
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAiCheck.setOnClickListener {
            findNavController().navigate(R.id.dest_image_result)
        }

        binding.btnCompositeCheck.setOnClickListener {
            findNavController().navigate(R.id.dest_image_result)
        }
    }
}