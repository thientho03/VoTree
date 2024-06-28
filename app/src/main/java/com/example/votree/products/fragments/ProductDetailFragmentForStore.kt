package com.example.votree.products.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.FragmentProductDetailForStoreBinding
import com.example.votree.products.adapters.ProductImageAdapterString
import com.example.votree.products.adapters.UserReviewAdapter
import com.example.votree.products.models.ProductReview
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.users.repositories.StoreRepository
import com.example.votree.utils.CustomToast
import com.example.votree.utils.ToastType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailFragmentForStore : Fragment() {

    private lateinit var binding: FragmentProductDetailForStoreBinding
    private val args: ProductDetailFragmentForStoreArgs by navArgs()
    private val firestore = FirebaseFirestore.getInstance()
    private val productRepository = ProductRepository(firestore)
    private lateinit var userReviewAdapter: UserReviewAdapter
    private lateinit var reviewRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailForStoreBinding.inflate(inflater, container, false)
        setupUI()
        return binding.root
    }

    private fun setupUI() {
        displayProductDetails()
        setupButtons()
        setupReviewAdapter()
        fetchAndDisplayReviews()
        displayShopDetails()
    }

    private fun displayProductDetails() {
        with(binding) {
            args.currentProduct.let { product ->
                productName.text = product.productName
                productPrice.text = getString(R.string.price_format, product.price)
                description.text = product.description
                productType.text = product.type.toString()
                suitEnvironment.text = product.suitEnvironment.toString()
                suitClimate.text = product.suitClimate.toString()
                productRating.text = product.averageRate.toString()
                productSoldQuantity.text = product.quantitySold.toString()

                val imageUris = product.imageUrl
                val imageAdapter = ProductImageAdapterString(imageUris)
                productImageViewPager.adapter = imageAdapter

                if (!product.active) {
                    hideBtn.text = "Unhide"
                    hideBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_unhide, 0, 0, 0)
                }
            }
        }
    }

    private fun displayShopDetails() {
        args.currentProduct.storeId.let { storeId ->
            val storeRepository = StoreRepository()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val store = storeRepository.fetchStore(storeId)
                    val numberOfProducts = storeRepository.getNumberOfProductsOfStore(storeId)
                    val averageRating = storeRepository.getAverageProductRating(storeId)

                    // Update the UI on the main thread
                    withContext(Dispatchers.Main) {
                        binding.storeName.text = store.storeName
                        binding.storeSoldProductsTv.text = "$numberOfProducts"
                        binding.storeRatingTv.text = averageRating.toString()

                        Glide.with(this@ProductDetailFragmentForStore)
                            .load(store.storeAvatar)
                            .centerCrop()
                            .placeholder(R.drawable.img_placeholder)
                            .into(binding.storeAvatarIv)
                    }
                } catch (e: Exception) {
                    Log.e("ProductDetailFragment", "Error fetching store details", e)
                    // Handle errors, possibly update the UI to show an error message
                }
            }
        }

    }

    private fun setupButtons() {
        with(binding) {
            updateBtn.setOnClickListener { navigateToUpdateProduct() }
            deleteBtn.setOnClickListener { confirmProductDeletion() }
            hideBtn.setOnClickListener {
                val shouldHide = binding.hideBtn.text.toString() == "Hide"
                toggleProductVisibility(shouldHide)
            }

            productDetailToolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }

            viewAllReviewBtn.setOnClickListener {
                gotoReviewsList()
            }

            storeInfo.setOnClickListener {
                val directions =
                    ProductDetailFragmentForStoreDirections.actionProductDetailFragmentForStoreToStoreProfile2(
                        args.currentProduct.storeId
                    )
                findNavController().navigate(directions)
            }
        }
    }

    private fun toggleProductVisibility(shouldHide: Boolean) {
        val actionWord = if (shouldHide) "hide" else "unhide"
        AlertDialog.Builder(requireContext())
            .setTitle("${actionWord.capitalize()} Product")
            .setMessage("Are you sure you want to $actionWord this product?")
            .setPositiveButton("Accept") { _, _ ->
                productRepository.toggleProductVisibility(args.currentProduct, onSuccess = {
                    CustomToast.show(requireContext(), "Product ${actionWord}d", ToastType.SUCCESS)
                    findNavController().popBackStack()
                }, onFailure = {
                    CustomToast.show(
                        requireContext(),
                        "Error ${actionWord}ing product",
                        ToastType.FAILURE
                    )
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToUpdateProduct() {
        val action =
            ProductDetailFragmentForStoreDirections.actionProductDetailFragmentForStoreToUpdateProduct(
                args.currentProduct
            )
        findNavController().navigate(action)
    }

    private fun confirmProductDeletion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Product")
            .setMessage("Are you sure you want to delete this product?")
            .setPositiveButton("Accept") { _, _ ->
                productRepository.deleteProduct(args.currentProduct) { success ->
                    if (success) findNavController().popBackStack()
                    else Log.e("ProductDetail", "Error deleting product")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupReviewAdapter() {
        reviewRecyclerView = binding.reviewRecyclerView
        reviewRecyclerView.layoutManager = LinearLayoutManager(context)
    }


    private fun fetchAndDisplayReviews() {
        val reviews = mutableListOf<ProductReview>()
        // Go to the products/productId/reviews collection in Firestore
        firestore.collection("products").document(args.currentProduct.id).collection("reviews")
            .orderBy("rating", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { reviewsSnapshot ->
                for (reviewDocument in reviewsSnapshot.documents) {
                    val review = reviewDocument.toObject(ProductReview::class.java)
                    review?.let { reviews.add(it) }
                }
                // Only take the first 2 reviews to display
                val reviewsToDisplay = reviews.take(2)
                userReviewAdapter =
                    UserReviewAdapter(reviewsToDisplay, CoroutineScope(Dispatchers.Main))
                reviewRecyclerView.adapter = userReviewAdapter

                binding.totalReviewTv.text = reviews.size.toString()
            }
            .addOnFailureListener { e ->
                Log.e("ProductDetail", "Error fetching reviews", e)
            }

        CoroutineScope(Dispatchers.IO).launch {
            val productRepository = ProductRepository(firestore)
            try {
                val productRating =
                    productRepository.getAverageProductRating(args.currentProduct.id)
                withContext(Dispatchers.Main) {
                    binding.totalRatingTv.text = productRating.toString()
                }
            } catch (e: Exception) {
                Log.e("ProductDetailFragment", "Error fetching product details", e)
            }
        }
    }

    private fun gotoReviewsList() {
        val action =
            ProductDetailFragmentDirections.actionProductDetailToProductReviewListFragment(args.currentProduct)
        findNavController().navigate(action)
    }
}