package com.example.votree.users.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.R
import com.example.votree.tips.AdManager
import com.example.votree.users.fragments.EmailVerificationFragment
import com.example.votree.utils.AuthHandler
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val emailVerificationFragment: EmailVerificationFragment?
        get() = supportFragmentManager.findFragmentById(R.id.forgotPassword_fcv) as? EmailVerificationFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val adView = findViewById<AdView>(R.id.adView)
        AdManager.addAdView(adView, this)

        // Initialize the AuthHandler
        auth = AuthHandler.firebaseAuth

        // Handle the password reset request
        emailVerificationFragment?.onResetPasswordRequested = { email ->
            sendPasswordResetEmail(email)
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    emailVerificationFragment?.notifyEmailSent()
                } else {
                    val message = task.exception?.message ?: "Failed to send reset email"
                    emailVerificationFragment?.notifyError(message)
                }
            }
    }
}