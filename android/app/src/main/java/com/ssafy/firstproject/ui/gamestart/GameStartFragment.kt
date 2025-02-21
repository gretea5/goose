package com.ssafy.firstproject.ui.gamestart

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseFragment
import com.ssafy.firstproject.databinding.FragmentGameStartBinding

class GameStartFragment : BaseFragment<FragmentGameStartBinding>(
    FragmentGameStartBinding::bind,
    R.layout.fragment_game_start
) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btn.setOnClickListener {
            findNavController().navigate(R.id.action_dest_game_start_to_dest_game)
        }
    }
}