package com.example.votree.users.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.votree.PurchaseActivity
import com.example.votree.R
import com.example.votree.databinding.FragmentUserProfileBinding
import com.example.votree.users.activities.OrderHistoryActivity
import com.example.votree.users.activities.RegisterToSeller
import com.example.votree.users.activities.SignInActivity
import com.example.votree.users.factories.UserProfileViewModelFactory
import com.example.votree.users.repositories.UserRepository
import com.example.votree.users.view_models.UserProfileViewModel
import com.example.votree.utils.ProgressDialogUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserProfileFragment : Fragment() {
    private val viewModel: UserProfileViewModel by viewModels {
        UserProfileViewModelFactory(UserRepository(FirebaseFirestore.getInstance()))
    }
    private var _binding: FragmentUserProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        loadUserProfileDetailsFragment()

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.userfullNameTv.text = user.fullName
                binding.userRoleTv.text = user.role
                Glide.with(requireContext())
                    .load(user.avatar)
                    .placeholder(R.drawable.avatar_default_2)
                    .into(binding.userAvatarIv)

                if (user.role == "store") {
                    disableBecomeSeller()
                }
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                ProgressDialogUtils.showLoadingDialog(requireContext())
                // Hide all the attributes in the view
                binding.userfullNameTv.visibility = View.GONE
                binding.userRoleTv.visibility = View.GONE
                binding.userAvatarIv.visibility = View.GONE
                binding.becomeSellerLayout.visibility = View.GONE
                binding.settingLayout.visibility = View.GONE
                binding.ordersLayout.visibility = View.GONE
                binding.pointsLayout.visibility = View.GONE
                binding.logoutBtn.visibility = View.GONE
            } else {
                ProgressDialogUtils.hideLoadingDialog()
                // Show all the attributes in the view
                binding.userfullNameTv.visibility = View.VISIBLE
                binding.userRoleTv.visibility = View.VISIBLE
                binding.userAvatarIv.visibility = View.VISIBLE
                binding.becomeSellerLayout.visibility = View.VISIBLE
                binding.settingLayout.visibility = View.VISIBLE
                binding.ordersLayout.visibility = View.VISIBLE
                binding.pointsLayout.visibility = View.VISIBLE
                binding.logoutBtn.visibility = View.VISIBLE
            }
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        viewModel.loadUserProfile(userId)
    }

    private fun disableBecomeSeller() {
        // Disable the becomeSeller layout
        binding.becomeSellerLayout.isEnabled = false
        // Change the text color to gray
        binding.becomeSellerTv.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.md_theme_outlineVariant)
        )
    }

    private fun loadUserProfileDetailsFragment() {
        val userProfileDetailsFragment = UserProfileDetailsFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.userProfileDetail_fcv, userProfileDetailsFragment)
            .commit()
    }

    private fun setupClickListeners() {
        binding.settingLayout.setOnClickListener {
            // Navigate to Settings
            navigateToSettings()
        }

        binding.ordersLayout.setOnClickListener {
            // Navigate to Orders
            navigateToOrders()
        }

        binding.becomeSellerLayout.setOnClickListener {
            // Navigate to Become Seller
            navigateToBecomeSeller()
        }

        binding.pointsLayout.setOnClickListener {
            // Navigate to Points Fragment
            navigateToAccumulatePoints()
        }

        binding.logoutBtn.setOnClickListener {
            // Navigate to Sign In Activity
            navigateLogout()
        }

        binding.paymentMethodBtn.setOnClickListener {
            val intent = Intent(context, PurchaseActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToSettings() {
        // Implement navigation logic to User Profile Settings Fragment
        val action = UserProfileFragmentDirections.actionUserProfileFragmentToUserProfileSettingFragment()
        findNavController().navigate(action)
    }

    private fun navigateToOrders() {
        val intent = Intent(context, OrderHistoryActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToBecomeSeller() {
        val intent = Intent(context, RegisterToSeller::class.java)
        startActivity(intent)
    }

    private fun navigateToAccumulatePoints() {
        // Implement navigation logic to Points Fragment
        val action =
            UserProfileFragmentDirections.actionUserProfileFragmentToPointTransactionFragment()
        findNavController().navigate(action)
    }

    private fun navigateLogout(){
        SignInActivity().signOut()
        val intent = Intent(this.context, SignInActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}