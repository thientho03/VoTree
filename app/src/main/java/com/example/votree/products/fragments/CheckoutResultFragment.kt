package com.example.votree.products.fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.votree.R
import com.example.votree.databinding.FragmentCheckoutResultBinding
import com.example.votree.users.activities.OrderHistoryActivity

class CheckoutResultFragment : Fragment() {
    private var _binding: FragmentCheckoutResultBinding? = null
    private val binding get() = _binding!!
    private val args: CheckoutResultFragmentArgs by navArgs()
    private var autoNavigateHandler: Handler? = null
    private var autoNavigateRunnable: Runnable? = null
    private var countdownSeconds = 5
    private var isCountdownActive = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateUI()
        if (!isCountdownActive) {
            startAutoNavigateTimer()
        } else {
            val action =
                CheckoutResultFragmentDirections.actionCheckoutResultFragmentToProductList()
            findNavController().navigate(action)
        }
    }

    private fun updateUI() {
        // Payment was successful
        binding.productImageIv.setImageResource(R.drawable.ic_checkout_sc)
        binding.pointsTv.text = "+ ${args.points} Points"
        binding?.orderHistoryBtn?.setOnClickListener {
            stopAutoNavigateTimer()
            startActivity(Intent(requireContext(), OrderHistoryActivity::class.java))
        }
        binding?.productListBtn?.setOnClickListener {
            stopAutoNavigateTimer()
            val action =
                CheckoutResultFragmentDirections.actionCheckoutResultFragmentToProductList()
            findNavController().navigate(action)
        }

        // Update the countdown text
//        if (!isCountdownActive) {
//            binding.countdownTv.text = "Redirecting to Product List in $countdownSeconds seconds"
//        } else {
//            binding.countdownTv.visibility = View.GONE
//        }
    }

    private fun startAutoNavigateTimer() {
        autoNavigateHandler = Handler(Looper.getMainLooper())
        autoNavigateRunnable = Runnable {
            val action =
                CheckoutResultFragmentDirections.actionCheckoutResultFragmentToProductList()
            findNavController().navigate(action)
        }
        autoNavigateHandler?.postDelayed(autoNavigateRunnable!!, 5200)
        updateCountdownText()
        isCountdownActive = true
    }

    private fun updateCountdownText() {
        binding.countdownTv.text = "Redirecting to Product List in $countdownSeconds seconds"
        countdownSeconds--
        if (countdownSeconds >= 0) {
            autoNavigateHandler?.postDelayed({
                updateCountdownText()
            }, 1000)
        } else {
            isCountdownActive = false
        }
    }

    private fun stopAutoNavigateTimer() {
        autoNavigateHandler?.removeCallbacks(autoNavigateRunnable!!)
        isCountdownActive = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoNavigateTimer()
    }
}