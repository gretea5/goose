package com.ssafy.firstproject.ui.record

import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ssafy.firstproject.R
import com.ssafy.firstproject.base.BaseFragment
import com.ssafy.firstproject.databinding.FragmentRecordBinding
import com.ssafy.firstproject.ui.record.adapter.UserNewsAdapter
import com.ssafy.firstproject.ui.record.viewmodel.RecordViewModel

class RecordFragment : BaseFragment<FragmentRecordBinding>(
    FragmentRecordBinding::bind,
    R.layout.fragment_record
) {

    private lateinit var adapter: UserNewsAdapter
    private val viewModel by viewModels<RecordViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        initAdapter()
        observeNewsList()
    }

    private fun initAdapter() {
        adapter = UserNewsAdapter { item ->
            val action = RecordFragmentDirections.actionDestRecordToDestCheckResultDetail(item)

            findNavController().navigate(action)
        }

        binding.rvNews.adapter = adapter
    }

    private fun observeNewsList() {
        viewModel.userNewsList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            setVisibility(it.isEmpty())
        }
    }

    private fun setVisibility(isEmpty: Boolean) {
        binding.rvNews.isGone = isEmpty
        binding.groupNoRecord.isVisible = isEmpty
    }
}