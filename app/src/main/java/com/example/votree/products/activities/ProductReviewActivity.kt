package com.example.votree.products.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.ActivityProductReviewBinding
import com.example.votree.products.models.ProductReview
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.products.repositories.ProductReviewRepository
import com.example.votree.utils.CustomToast
import com.example.votree.utils.ToastType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ProductReviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductReviewBinding
    private var imageUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val productReviewRepository = ProductReviewRepository(firestore)
    private val productRepository = ProductRepository(firestore)
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductReviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupButton()
        checkReviewSubmitted()
        setupNavigation()
    }

    private fun setupNavigation() {
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.arrow_back_24px)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.toolbar.title = getString(R.string.product_review)
    }

    private fun setupButton() {
        binding.addImageButton.setOnClickListener { pickImage() }
        binding.submitReviewButton.setOnClickListener { submitReview() }
    }

    private fun pickImage() {
        if (hasImagePermission()) {
            Log.d("ProductReviewActivity", "Picking image")
            val pickImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            getImage.launch(pickImageIntent)
        } else {
            requestImagePermission()
        }
    }

    private fun hasImagePermission() = ContextCompat.checkSelfPermission(
        this, android.Manifest.permission.READ_MEDIA_IMAGES
    ) == PackageManager.PERMISSION_GRANTED

    private fun requestImagePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                Log.d("ProductReviewActivity", "Requesting permission")
                requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), IMAGE_REQUEST_CODE)
            }
        }
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.productImageView.setImageURI(imageUri)
        }
    }

    private fun checkReviewSubmitted() {
        val isSubmitted = intent.getBooleanExtra("isSubmitted", false)
        val transactionId = intent.getStringExtra("transactionId") ?: return

        if (isSubmitted) {
            lifecycleScope.launch {
                binding.submitReviewButton.text = "Update Review"
                productReviewRepository.fetchReviewByTransactionId(transactionId, userId)?.let { review ->
                    with(binding) {
                        reviewEditText.setText(review.reviewText)
                        ratingBar.rating = review.rating
                        imageUri = Uri.parse(review.imageUrl)
                        Glide.with(this@ProductReviewActivity).load(imageUri).into(productImageView)
                        productImageView.setImageURI(imageUri)
                    }
                } ?: CustomToast.show(this@ProductReviewActivity, "Failed to fetch review", ToastType.FAILURE)
            }
        }
    }

    private fun submitReview() {
        val reviewText = binding.reviewEditText.text.toString()
        val rating = binding.ratingBar.rating
        val transactionId = intent.getStringExtra("transactionId") ?: return

        imageUri?.let { uri ->
            lifecycleScope.launch {
                try {
                    productRepository.uploadProductImage(uri, { imageUrl ->
                        val review = ProductReview("", transactionId, userId, reviewText, rating, imageUrl)
                        if (intent.getBooleanExtra("isSubmitted", false)) {
                            updateReview(review)
                        } else {
                            addNewReview(review)
                        }
                    }, { e ->
                        CustomToast.show(this@ProductReviewActivity, "Failed to upload image: ${e.message}", ToastType.FAILURE)
                    })

                } catch (e: Exception) {
                    CustomToast.show(this@ProductReviewActivity, "Failed to submit review: ${e.message}", ToastType.FAILURE)
                }
            }
        }
    }

    private fun updateReview(review: ProductReview) {
        productReviewRepository.updateReview(review, onSuccess = {
            CustomToast.show(this, "Review updated successfully", ToastType.SUCCESS)
            finish()
        }, onFailure = {
            CustomToast.show(this, "Failed to update review: ${it.message}", ToastType.FAILURE)
        })
    }

    private fun addNewReview(review: ProductReview) {
        productReviewRepository.addProductReview(review, review.transactionId, onSuccess = {
            CustomToast.show(this@ProductReviewActivity, "Review submitted successfully", ToastType.SUCCESS)
            finish()
        }, onFailure = {
            CustomToast.show(this@ProductReviewActivity, "Failed to submit review: ${it.message}", ToastType.FAILURE)
        })
    }

    companion object {
        const val IMAGE_REQUEST_CODE = 1001
    }
}