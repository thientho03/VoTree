package com.example.votree.products.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.votree.R
import com.example.votree.databinding.FragmentProductDetailBinding
import com.example.votree.products.adapters.ProductImageAdapterString
import com.example.votree.products.adapters.UserReviewAdapter
import com.example.votree.products.models.Cart
import com.example.votree.products.models.ProductReview
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.products.view_models.CartViewModel
import com.example.votree.users.activities.ChatActivity
import com.example.votree.users.activities.MessageActivity
import com.example.votree.users.repositories.StoreRepository
import com.example.votree.users.repositories.UserRepository
import com.example.votree.utils.FirebaseRealtime
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProductDetailFragment : Fragment() {

    private lateinit var binding: FragmentProductDetailBinding
    private val args: ProductDetailFragmentArgs by navArgs()
    private val cartViewModel = CartViewModel()
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var userReviewAdapter: UserReviewAdapter
    private lateinit var reviewRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        setupUI()

        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid
        val storeUid = args.currentProduct.storeId

        if (uid != null) {
            CoroutineScope(Dispatchers.IO).launch {
                val userRepository = UserRepository(FirebaseFirestore.getInstance())
                val uidOfStore = userRepository.getUserByStoreId(storeUid)

                if (uidOfStore != null) {
                    FirebaseRealtime.getInstance().addFriend(uid, uidOfStore.id)
                }
            }
        }

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
        CoroutineScope(Dispatchers.IO).launch {
            val productRepository = ProductRepository(firestore)
            try {
                val productRating =
                    productRepository.getAverageProductRating(args.currentProduct.id)
                withContext(Dispatchers.Main) {
                    binding.productRatingRb.rating = productRating
                    binding.productRating.text = productRating.toString()
                }
            } catch (e: Exception) {
                Log.e("ProductDetailFragment", "Error fetching product details", e)

            }
        }

        with(binding) {
            args.currentProduct.let { product ->
                productName.text = product.productName
                productPrice.text = getString(R.string.price_format, product.price)
                description.text = product.description
                productType.text = product.type.toString()
                suitEnvironment.text = product.suitEnvironment.toString()
                suitClimate.text = product.suitClimate.toString()
                productSoldQuantity.text = product.quantitySold.toString()

                val imageUris = product.imageUrl
                val imageAdapter = ProductImageAdapterString(imageUris)
                productImageViewPager.adapter = imageAdapter
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

                        Glide.with(requireContext())
                            .load(store.storeAvatar)
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
            productDetailToolbar.setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            productDetailToolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.productDetail_to_StoreReport -> {
                        val action =
                            ProductDetailFragmentDirections.actionProductDetailToStoreReport(args.currentProduct.storeId)
                        findNavController().navigate(action)
                        true
                    }

                    R.id.productDetail_to_ProductReport -> {
                        val action =
                            ProductDetailFragmentDirections.actionProductDetailToProductReport(args.currentProduct.id)
                        findNavController().navigate(action)
                        true
                    }

                    else -> false
                }
            }
            buyNowBtn.setOnClickListener {
                gotoCheckout()
            }
            addToCartBtn.setOnClickListener {
                cartViewModel.addProductToCart(args.currentProduct.id, 1)
                showSnackbar("Product added to cart")
            }

            viewAllReviewBtn.setOnClickListener {
                gotoReviewsList()
            }

            storeInfo.setOnClickListener {
                val directions =
                    ProductDetailFragmentDirections.actionProductDetailToStoreProfile2(args.currentProduct.storeId)
                findNavController().navigate(directions)
            }

            chattingBtn.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val userRepository = UserRepository(FirebaseFirestore.getInstance())
                    val storeId = args.currentProduct.storeId
                    val store = userRepository.getUserByStoreId(storeId)
                    val intent = Intent(context, MessageActivity::class.java).apply {
                        if (store != null) {
                            putExtra("FRIEND_ID", store.id)
                        }
                        if (store != null) {
                            putExtra("FRIEND_NAME", store.username)
                        }
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT)
            .setAction("View Cart") {
                val action = ProductDetailFragmentDirections.actionProductDetailToCartList()
                findNavController().navigate(action)
            }
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

    private fun gotoCheckout() {
        val cartWithSelectedProduct = Cart(
            id = "",
            userId = "",
            productsMap = mutableMapOf(args.currentProduct.id to 1),
            totalPrice = 0.0
        )

        val action =
            ProductDetailFragmentDirections.actionProductDetailToCheckout(cartWithSelectedProduct)
        findNavController().navigate(action)
    }
}