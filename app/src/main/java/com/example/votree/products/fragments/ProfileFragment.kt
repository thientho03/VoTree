package com.example.votree.products.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.FragmentProfileBinding
import com.example.votree.users.models.User
import com.example.votree.users.repositories.UserRepository
import com.example.votree.utils.ProgressDialogUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_CODE_PICK_IMAGE = 123
    private var avatarUri: Uri = Uri.EMPTY

    private val userRepository by lazy {
        UserRepository(FirebaseFirestore.getInstance())
    }

    private val currentUserId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
        setupClickListeners()
    }

    private fun loadUserData() {
        viewLifecycleOwner.lifecycleScope.launch {
            val user = userRepository.getUser(currentUserId)
            user?.let { updateUi(it) }
        }
    }

    private fun updateUi(user: User) {
        with(binding) {
            Glide.with(this@ProfileFragment)
                .load(user.avatar)
                .placeholder(R.drawable.avatar_default_2)
                .into(productImageView)
            userNameEt.setText(user.username)
            fullNameEt.setText(user.fullName)
            emailEt.setText(user.email)
            addressEt.setText(user.address)
            phoneNumberEt.setText(user.phoneNumber)
        }
    }

    private fun setupClickListeners() {
        binding.changeAvatarBtn.setOnClickListener {
            pickAvatarImage()
        }

        binding.saveBtn.setOnClickListener {
            updateUserData(avatarUri)
        }
    }

    private fun pickAvatarImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                avatarUri = uri
                Glide.with(this)
                    .load(uri)
                    .into(binding.productImageView)
            }
        }
    }

    private fun updateUserData(avatarUri: Uri?) {
        viewLifecycleOwner.lifecycleScope.launch {
            ProgressDialogUtils.showLoadingDialog(requireContext())
            val currentUser = userRepository.getUser(currentUserId)
            currentUser?.let { user ->
                val updatedUser = user.copy(
                    username = binding.userNameEt.text.toString(),
                    fullName = binding.fullNameEt.text.toString(),
                    email = binding.emailEt.text.toString(),
                    address = binding.addressEt.text.toString(),
                    phoneNumber = binding.phoneNumberEt.text.toString()
                )

                val newAvatarUrl = avatarUri?.let { uri ->
                    if (uri != Uri.parse(user.avatar)) {
                        userRepository.uploadAvatar(currentUserId, uri)
                    } else {
                        user.avatar
                    }
                } ?: user.avatar

                updatedUser.avatar = newAvatarUrl
                userRepository.updateUser(currentUserId, updatedUser)
                ProgressDialogUtils.hideLoadingDialog()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}