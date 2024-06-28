package com.example.votree.users.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.votree.databinding.FragmentUserProfileDetailsBinding
import com.example.votree.products.repositories.PointTransactionRepository
import com.example.votree.products.repositories.ShippingAddressRepository
import com.example.votree.users.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class UserProfileDetailsFragment : Fragment() {
    private var _binding: FragmentUserProfileDetailsBinding? = null
    private val binding get() = _binding!!

    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userRepository = UserRepository(FirebaseFirestore.getInstance())
        val shippingAddressRepository = ShippingAddressRepository(
            FirebaseFirestore.getInstance(),
            FirebaseAuth.getInstance()
        )
        val pointTransactionRepository = PointTransactionRepository()

        lifecycleScope.launch {
            val user = userRepository.getUser(userId)
            val address = shippingAddressRepository.getDefaultShippingAddress()
            user?.let {
                binding.userNameTv.text = it.username
                binding.userEmailTv.text = it.email
                binding.userPhoneNumberTv.text = it.phoneNumber
                binding.userAddressTv.text = address?.recipientAddress ?: ""
                binding.createdDateTv.text = it.createdAt.toString()

                // Fetch and update accumulated points within the same scope
                val userPoints = pointTransactionRepository.getCurrentPoints()
                binding.accumulatePointsTv.text = userPoints.toString()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}