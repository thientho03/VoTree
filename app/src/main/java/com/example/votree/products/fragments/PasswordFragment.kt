package com.example.votree.products.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.votree.databinding.FragmentPasswordBinding
import com.example.votree.utils.CustomToast
import com.example.votree.utils.ToastType
import com.google.firebase.Firebase
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class PasswordFragment : Fragment() {

    private var _binding: FragmentPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPasswordBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Authentication
        auth = Firebase.auth

        // Set click listener for the save button
        binding.saveBtn.setOnClickListener {
            updatePassword()
        }
    }

    private fun updatePassword() {
        val currentPassword = binding.currentPasswordEt.text.toString().trim()
        val newPassword = binding.newPasswordEt.text.toString().trim()
        val confirmNewPassword = binding.confirmNewPasswordEt.text.toString().trim()

        // Validate the input
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            CustomToast.show(requireContext(), "Please fill all the fields", ToastType.FAILURE)
            return
        }

        if (newPassword != confirmNewPassword) {
            CustomToast.show(requireContext(), "Passwords do not match", ToastType.FAILURE)
            return
        }

        // Get the current user
        val user = auth.currentUser

        // Reauthenticate the user with the current password
        val credential = EmailAuthProvider.getCredential(user?.email ?: "", currentPassword)
        user?.reauthenticate(credential)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Update the password
                user.updatePassword(newPassword)
                    .addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            CustomToast.show(requireContext(), "Password updated successfully", ToastType.SUCCESS)
                            findNavController().popBackStack()
                        } else {
                            CustomToast.show(requireContext(), "Failed to update password", ToastType.FAILURE)
                        }
                    }
            } else {
                CustomToast.show(requireContext(), "Failed to check your current password", ToastType.FAILURE)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}