package com.example.votree.products.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.setFragmentResult
import com.example.votree.databinding.FragmentReviewFilterBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ReviewFilterBottomSheetFragment : BottomSheetDialogFragment() {
    private var _binding: FragmentReviewFilterBottomSheetBinding? = null
    private val binding get() = _binding!!
    private val selectedRatings = mutableSetOf<Float>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReviewFilterBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRadioButtons()
        setupButtons()
    }

    private fun setupRadioButtons() {
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            selectedRatings.clear()
            when (checkedId) {
                binding.oneStarRadio.id -> selectedRatings.add(1f)
                binding.twoStarsRadio.id -> selectedRatings.add(2f)
                binding.threeStarsRadio.id -> selectedRatings.add(3f)
                binding.fourStarsRadio.id -> selectedRatings.add(4f)
                binding.fiveStarsRadio.id -> selectedRatings.add(5f)
            }
        }
    }

    private fun setupButtons() {
        binding.btnResetAll.setOnClickListener {
            binding.radioGroup.clearCheck()
            selectedRatings.clear()
            dismiss()
        }
        binding.btnApply.setOnClickListener {
            val result = Bundle().apply {
                putFloatArray("selectedRatings", selectedRatings.toFloatArray())
            }
            setFragmentResult("requestKey", result)
            dismiss()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}