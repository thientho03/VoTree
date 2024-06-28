package com.example.votree.users.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.votree.databinding.FragmentEmailVerificationBinding

class EmailVerificationFragment : Fragment() {

    private var _binding: FragmentEmailVerificationBinding? = null
    private val binding get() = _binding!!

    // Callbacks to interact with the activity
    var onResetPasswordRequested: ((String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmailVerificationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSubmitButton()
    }

    private fun setupSubmitButton() {
        binding.submitEmailBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            if (email.isValidEmail()) {
                onResetPasswordRequested?.invoke(email)
            } else {
                binding.emailTil.error = "Please enter a valid email address"
            }
        }
    }

    fun notifyEmailSent() {
        Toast.makeText(context, "Reset link sent to your email address", Toast.LENGTH_LONG).show()
    }

    fun notifyError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun String.isValidEmail(): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}