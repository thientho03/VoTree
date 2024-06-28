package com.example.votree.users.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.MainActivity
import com.example.votree.R
import com.example.votree.admin.activities.AdminMainActivity
import com.example.votree.databinding.ActivitySignInBinding
import com.example.votree.tips.AdManager
import com.example.votree.utils.CustomToast
import com.example.votree.utils.ToastType
import com.google.android.gms.ads.AdView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFirebaseAuth()
        setupGoogleSignIn()
        setupUIListeners()

        val adView = findViewById<AdView>(R.id.adView)
        AdManager.addAdView(adView, this)
    }

    private fun setupFirebaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupUIListeners() {
        binding.textView2.setOnClickListener {
            navigateToSignUp()
        }

        binding.button.setOnClickListener {
            performSignIn()
        }

        binding.btnGoogle.setOnClickListener {
            signInGoogle()
        }

        binding.forgotPassword.setOnClickListener {
            navigateToForgotPassword()
        }
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun performSignIn() {
        val email = binding.emailEt.text.toString()
        val pass = binding.passET.text.toString()

        if (email.isNotEmpty() && pass.isNotEmpty()) {
            signInWithEmail(email, pass)
        } else {
            Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun signInWithEmail(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                checkUserAccessLevel(firebaseAuth.currentUser?.uid)
            } else {
                // Use CustomToast to show the error message
                val errorMessage = when {
                    task.exception is FirebaseAuthInvalidCredentialsException -> "The password is incorrect."
                    task.exception is FirebaseAuthInvalidUserException -> "The email address is not registered."
                    else -> "Sign in failed: ${task.exception?.message}"
                }
                CustomToast.show(this, errorMessage, ToastType.FAILURE)
            }
        }
    }

    private fun checkUserAccessLevel(uid: String?) {
        uid?.let {
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(it).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    navigateToMainActivity(document.getString("email")!!)
                } else {
                    checkAdminAccess(it)
                }
            }
        }
    }

    private fun checkAdminAccess(uid: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("admins").document(uid).get().addOnSuccessListener { document ->
            if (document.exists()) {
                navigateToAdminMainActivity(document.getString("email")!!)
            }
        }
    }

    private fun navigateToForgotPassword() {
        val intent = Intent(this, ForgotPasswordActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMainActivity(email: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("email", email)
        intent.putExtra("name", "User using email")
        startActivity(intent)
    }

    private fun navigateToAdminMainActivity(email: String) {
        val intent = Intent(this, AdminMainActivity::class.java)
        intent.putExtra("email", email)
        intent.putExtra("name", "Admin using email")
        startActivity(intent)
    }

    private fun signInGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task)
        }
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            val account: GoogleSignInAccount? = task.result
            account?.let {
                firebaseAuthWithGoogle(it)
            }
        } else {
            // Use CustomToast to show the error message
            CustomToast.show(this, "Google sign in failed: ${task.exception?.message}", ToastType.FAILURE)
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateUI(account)
            } else {
                Toast.makeText(this, task.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(account: GoogleSignInAccount) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("email", account.email)
        intent.putExtra("name", account.displayName)
        startActivity(intent)
        CustomToast.show(this, "Sign in with Google successful!", ToastType.SUCCESS)
    }

    fun signOut() {
        if (::firebaseAuth.isInitialized) {
            firebaseAuth.signOut()
        }
        if (::googleSignInClient.isInitialized) {
            googleSignInClient.signOut()
        }
    }
}