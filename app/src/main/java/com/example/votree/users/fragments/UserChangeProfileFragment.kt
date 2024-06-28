package com.example.votree.users.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.votree.R
import com.example.votree.databinding.FragmentUserChangeProfileBinding
import com.example.votree.users.repositories.UserRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class UserChangeProfileFragment : Fragment() {

    private var _binding: FragmentUserChangeProfileBinding? = null
    private val binding get() = _binding!!
    private val curUser = FirebaseAuth.getInstance().currentUser
    private val userId = curUser?.uid ?: ""
    private val userRepository = UserRepository(FirebaseFirestore.getInstance())
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserChangeProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val updateButton = view.findViewById<Button>(R.id.update_btn)

        updateButton.setOnClickListener {
            updateUser()
        }

        loadUserData()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val user = userRepository.getUser(userId)
            user?.let {
                binding.userNameEt.setText(it.username as String)
                binding.userFullNameEt.setText(it.fullName as String)
                binding.userPhoneNumberEt.setText(it.phoneNumber as String)
                binding.userAddressEt.setText(it.address as String)
            }
        }
    }

    private fun updateUser() {
        lifecycleScope.launch {
            val user = userRepository.getUser(userId)
            user?.let setOnClickListener@{
                it.username = binding.userNameEt.text.toString()
                it.fullName = binding.userFullNameEt.text.toString()
                it.phoneNumber = binding.userPhoneNumberEt.text.toString()
                it.address = binding.userAddressEt.text.toString()

                val curPassword = binding.curPasswordEt.text.toString();
                val newPassword = binding.newPasswordEt.text.toString();
                val conPassword = binding.conPasswordEt.text.toString();

                if (!curPassword.isEmpty() || !newPassword.isEmpty() || !conPassword.isEmpty()) {
                    if (curPassword == user.password) {
                        if (newPassword == conPassword) {
                            it.password = newPassword
                            curUser?.updatePassword(newPassword)
                        } else {
                            view?.let { it1 ->
                                Snackbar.make(it1, "Passwords do not match", Snackbar.LENGTH_SHORT).show()
                            }
                            return@setOnClickListener
                        }
                    } else {
                        view?.let { it1 ->
                            Snackbar.make(it1, "Password incorrect", Snackbar.LENGTH_SHORT).show()
                        }
                        return@setOnClickListener
                    }
                }

                try {
                    userRepository.updateUser(userId, user)
                    view?.let { it1 ->
                        Snackbar.make(it1, "User data updated successfully", Snackbar.LENGTH_SHORT)
                            .show()
                        findNavController().popBackStack()
                    }
                } catch (e: Exception) {
                    view?.let { it1 ->
                        Snackbar.make(
                            it1,
                            "Failed to update user data: ${e.message}",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
}