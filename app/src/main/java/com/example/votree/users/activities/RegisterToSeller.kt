package com.example.votree.users.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.R
import com.example.votree.databinding.ActivityRegisterToSellerBinding
import com.example.votree.tips.AdManager
import com.example.votree.users.models.Store
import com.example.votree.users.repositories.StoreRepository
import com.example.votree.users.repositories.UserRepository
import com.example.votree.utils.CustomToast
import com.example.votree.utils.ProgressDialogUtils
import com.example.votree.utils.ToastType
import com.example.votree.utils.ValidationUtils
import com.google.android.gms.ads.AdView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class RegisterToSeller : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterToSellerBinding
    private val storeRepository = StoreRepository()
    private val storageReference = FirebaseStorage.getInstance().getReference("images/storeAvatars")
    private var avatarUri = Uri.EMPTY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterToSellerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButton()
        setupAddress()

        val adView = findViewById<AdView>(R.id.adView)
        AdManager.addAdView(adView, this)
    }

    // Handle the address result from AddressActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADDRESS_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the address from the result
            val address = data?.getStringExtra("address")
            binding.etShopAddress.setText(address)
        }
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            binding.shopAvatarIv.setImageURI(uri)
            avatarUri = uri
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.etShopName.text.toString().trim().isEmpty()) {
            binding.etShopName.error = "Shop name is required"
            isValid = false
        }

        if (binding.etShopAddress.text.toString().trim().isEmpty()) {
            binding.etShopAddress.error = "Shop address is required"
            isValid = false
        }

        val email = binding.etShopEmail.text.toString().trim()
        val phoneNumber = binding.etShopPhone.text.toString().trim()

        if (email.isEmpty()) {
            binding.etShopEmail.error = "Email is required"
            isValid = false
        } else if (!ValidationUtils.isValidEmail(email)) {
            binding.etShopEmail.error = "Invalid email format"
            isValid = false
        }

        if (phoneNumber.isEmpty()) {
            binding.etShopPhone.error = "Phone number is required"
            isValid = false
        } else if (!ValidationUtils.isValidPhoneNumber(phoneNumber)) {
            binding.etShopPhone.error = "Phone number must be 10 digits"
            isValid = false
        }

        return isValid
    }

    private fun setupButton() {
        binding.btnRegister.setOnClickListener {
            uploadImageToFirebase(avatarUri)
        }

        // Set up the button click listener
        binding.uploadAvatarBtn.setOnClickListener {
            // Launch the image picker
            getImage.launch("image/*")
        }
    }

    private fun createNewStore(avatarUrl: String) {
        if (validateForm()) {
            val store = Store(
                id = "",
                storeName = binding.etShopName.text.toString(),
                storeLocation = binding.etShopAddress.text.toString(),
                storeEmail = binding.etShopEmail.text.toString(),
                storePhoneNumber = binding.etShopPhone.text.toString(),
                storeAvatar = avatarUrl
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val userId = Firebase.auth.currentUser?.uid ?: ""
                    storeRepository.createNewStore(store, userId)

                    val userRepository = UserRepository(Firebase.firestore)
                    userRepository.updateToStore(userId, store.id)
                    userRepository.updateAvatar(userId, avatarUrl)

                    runOnUiThread {
                        CustomToast.show(this@RegisterToSeller, "Store created successfully", ToastType.SUCCESS)
                        SignInActivity().signOut()
                        val intent = Intent(this@RegisterToSeller, SignInActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        CustomToast.show(this@RegisterToSeller, "Failed to create store: ${e.message}", ToastType.FAILURE)
                    }
                }
            }
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri) {
        ProgressDialogUtils.showLoadingDialog(this)

        val fileName = UUID.randomUUID().toString() + ".jpg"
        val fileRef = storageReference.child(fileName)
        val uploadTask = fileRef.putFile(fileUri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            fileRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                createNewStore(downloadUri.toString())
                ProgressDialogUtils.hideLoadingDialog()
            } else {
                CustomToast.show(this, "Failed to upload image", ToastType.FAILURE)
            }
        }
    }

    private fun setupAddress() {
        binding.etShopAddress.setOnClickListener {
            val intent = Intent(this, AddressActivity::class.java)
            startActivityForResult(intent, ADDRESS_REQUEST_CODE)
        }
    }

    companion object {
        const val ADDRESS_REQUEST_CODE = 1111
        const val REGISTER_TO_SELLER_CODE = 1110
    }
}
