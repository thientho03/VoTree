//package com.example.votree.products.fragments
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ArrayAdapter
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.navigation.fragment.navArgs
//import com.bumptech.glide.Glide
//import com.example.votree.R
//import com.example.votree.databinding.FragmentUpdateProductBinding
//import com.example.votree.products.data.productCatagories.PlantType
//import com.example.votree.products.data.productCatagories.SuitClimate
//import com.example.votree.products.data.productCatagories.SuitEnvironment
//import com.example.votree.products.models.Product
//import com.example.votree.products.repositories.ProductRepository
//import com.google.android.material.textfield.MaterialAutoCompleteTextView
//import com.google.firebase.firestore.FirebaseFirestore
//
//class UpdateProduct : Fragment() {
//    private var _binding: FragmentUpdateProductBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var productRepository: ProductRepository
//    private val args by navArgs<UpdateProductArgs>()
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentUpdateProductBinding.inflate(inflater, container, false)
//        productRepository = ProductRepository(FirebaseFirestore.getInstance())
//
//        setupViews()
//        setupSpinners()
//        setupSaveButton()
//
//        return binding.root
//    }
//
//    private fun setupSpinners() {
//        val plantType = PlantType.entries.toTypedArray()
//        val suitEnvironment = SuitEnvironment.entries.toTypedArray()
//        val suitClimate = SuitClimate.entries.toTypedArray()
//
//        val plantTypeAdapter =
//            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, plantType)
//        val suitEnvironmentAdapter =
//            ArrayAdapter(
//                requireContext(),
//                android.R.layout.simple_dropdown_item_1line,
//                suitEnvironment
//            )
//        val suitClimateAdapter =
//            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suitClimate)
//
//        (binding.suitClimateSpinner as? MaterialAutoCompleteTextView)?.setAdapter(suitClimateAdapter)
//        (binding.suitEnvironmentSpinner as? MaterialAutoCompleteTextView)?.setAdapter(
//            suitEnvironmentAdapter
//        )
//        (binding.typeSpinner as? MaterialAutoCompleteTextView)?.setAdapter(plantTypeAdapter)
//    }
//
//    private fun setupSaveButton() {
//        binding.saveProductBtn.setOnClickListener {
//            val product = Product(
//                id = args.currentProduct.id,
//                storeId = args.currentProduct.storeId,
//                productName = binding.productNameEt.text.toString(),
//                shortDescription = binding.shortDescriptionEt.text.toString(),
//                description = binding.descriptionEt.text.toString(),
//                price = binding.priceEt.text.toString().toDouble(),
//                inventory = binding.quantityEt.text.toString().toInt(),
//                type = binding.typeSpinner.text.toString().let { PlantType.valueOf(it) },
//                suitEnvironment = binding.suitEnvironmentSpinner.text.toString()
//                    .let { SuitEnvironment.valueOf(it) },
//                suitClimate = binding.suitClimateSpinner.text.toString()
//                    .let { SuitClimate.valueOf(it) },
//                saleOff = binding.saleOffEt.text.toString().toDouble(),
////                imageUrl = binding.productImageIv.toString()
//                imageUrl = args.currentProduct.imageUrl
//            )
//
//            productRepository.updateProduct(product) {
//                Toast.makeText(requireContext(), "Product updated successfully", Toast.LENGTH_SHORT)
//                    .show()
//                findNavController().popBackStack()
//            }
//        }
//    }
//
//    private fun setupViews() {
//        val product = args.currentProduct
//        binding.productNameEt.setText(product.productName)
//        binding.shortDescriptionEt.setText(product.shortDescription)
//        binding.descriptionEt.setText(product.description)
//        binding.priceEt.setText(product.price.toString())
//        binding.quantityEt.setText(product.inventory.toString())
//        binding.saleOffEt.setText(product.saleOff.toString())
//        binding.typeSpinner.setText(product.type.toString(), false)
//        binding.suitEnvironmentSpinner.setText(product.suitEnvironment.toString(), false)
//        binding.suitClimateSpinner.setText(product.suitClimate.toString(), false)
//
//        Glide.with(this)
//            .load(product.imageUrl)
//            .placeholder(R.drawable.img_placeholder)
//            .into(binding.productImageIv)
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
package com.example.votree.products.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.votree.R
import com.example.votree.databinding.FragmentUpdateProductBinding
import com.example.votree.products.adapters.ProductImageAdapter
import com.example.votree.products.adapters.ProductImageAdapterString
import com.example.votree.products.adapters.ProductImageAdapterUri
import com.example.votree.products.data.productCatagories.PlantType
import com.example.votree.products.data.productCatagories.SuitClimate
import com.example.votree.products.data.productCatagories.SuitEnvironment
import com.example.votree.products.repositories.ProductRepository
import com.example.votree.utils.ProgressDialogUtils
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val IMAGE_REQUEST_CODE = 100

class UpdateProduct : Fragment() {
    private var _binding: FragmentUpdateProductBinding? = null
    private val binding get() = _binding!!
    private lateinit var productRepository: ProductRepository
    private val args by navArgs<UpdateProductArgs>()

    private val imageUris = mutableListOf<Uri>()
    private lateinit var imageAdapter: ProductImageAdapter<Uri>

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUpdateProductBinding.inflate(inflater, container, false)
        productRepository = ProductRepository(FirebaseFirestore.getInstance())

        setupViews()
        setupSpinners()
        setupSaveButton()
        setupAddImageButton()

        // Set the navigation icon to the back button
        binding.toolbar.navigationIcon =
            ContextCompat.getDrawable(requireContext(), R.drawable.arrow_back_24px)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.toolbar.title = getString(R.string.update_product)

        return binding.root
    }

    private fun setupSpinners() {
        val plantType = PlantType.entries.toTypedArray()
        val suitEnvironment = SuitEnvironment.entries.toTypedArray()
        val suitClimate = SuitClimate.entries.toTypedArray()

        val plantTypeAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, plantType)
        val suitEnvironmentAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                suitEnvironment
            )
        val suitClimateAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, suitClimate)

        (binding.suitClimateSpinner as? MaterialAutoCompleteTextView)?.setAdapter(suitClimateAdapter)
        (binding.suitEnvironmentSpinner as? MaterialAutoCompleteTextView)?.setAdapter(
            suitEnvironmentAdapter
        )
        (binding.typeSpinner as? MaterialAutoCompleteTextView)?.setAdapter(plantTypeAdapter)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getImageFromDevice() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupAddImageButton() {
        binding.addImageBtn.setOnClickListener {
            getImageFromDevice()
        }
    }

    //    private fun setupSaveButton() {
//        binding.saveProductBtn.setOnClickListener {
//            viewLifecycleOwner.lifecycleScope.launch {
//                try {
//                    ProgressDialogUtils.showLoadingDialog(requireContext())
//                    val productName = binding.productNameEt.text.toString()
//                    val shortDescription = binding.shortDescriptionEt.text.toString()
//                    val description = binding.descriptionEt.text.toString()
//                    val price = binding.priceEt.text.toString().toDouble()
//                    val inventory = binding.quantityEt.text.toString().toInt()
//                    val type = binding.typeSpinner.text.toString().let { PlantType.valueOf(it) }
//                    val suitEnvironment = binding.suitEnvironmentSpinner.text.toString()
//                        .let { SuitEnvironment.valueOf(it) }
//                    val suitClimate =
//                        binding.suitClimateSpinner.text.toString().let { SuitClimate.valueOf(it) }
//                    val saleOff = binding.saleOffEt.text.toString().toDouble()
//
//                    // Create updated product with all fields of the args.currentProduct
//                    var updatedProduct = args.currentProduct.copy()
//
//                    if (imageUris.isNotEmpty() && imageUris != args.currentProduct.imageUrl.map {
//                            Uri.parse(
//                                it
//                            )
//                        }) {
//                        withContext(Dispatchers.IO) {
//                            var count = 0
//                            val imageUrls = mutableListOf<String>()
//                            for (imageUri in imageUris) {
//                                val imageUrl = productRepository.uploadProductImage(imageUri,
//                                    onSuccess = { url ->
//                                        imageUrls.add(url)
//                                        count++
//                                    },
//                                    onFailure = { e ->
//                                        Log.e(
//                                            "UpdateProduct",
//                                            "Error uploading image: ${e.message}",
//                                            e
//                                        )
//                                    }
//                                )
//
//                                if (count == imageUris.size) {
//                                    updatedProduct = updatedProduct.copy(
//                                        storeId = args.currentProduct.storeId,
//                                        imageUrl = imageUrls,
//                                        productName = productName,
//                                        shortDescription = shortDescription,
//                                        description = description,
//                                        price = price,
//                                        inventory = inventory,
//                                        type = type,
//                                        suitEnvironment = suitEnvironment,
//                                        suitClimate = suitClimate,
//                                        saleOff = saleOff
//                                    )
//                                    productRepository.updateProduct(updatedProduct,
//                                        onSuccess = {
//                                            Log.d("UpdateProduct", "Product updated successfully")
//
//                                        },
//                                        onFailure = { e ->
//                                            Toast.makeText(
//                                                requireContext(),
//                                                "Error: ${e.message}",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                            Log.e(
//                                                "UpdateProduct",
//                                                "Error updating product: ${e.message}",
//                                                e
//                                            )
//                                        }
//                                    )
//                                }
//                            }
//                        }
//                    } else {
//                        updatedProduct = updatedProduct.copy(
//                            productName = productName,
//                            shortDescription = shortDescription,
//                            description = description,
//                            price = price,
//                            inventory = inventory,
//                            type = type,
//                            suitEnvironment = suitEnvironment,
//                            suitClimate = suitClimate,
//                            saleOff = saleOff
//                        )
//                        productRepository.updateProduct(updatedProduct,
//                            onSuccess = {
//                                Log.d("UpdateProduct", "Product updated successfully")
//
//                            },
//                            onFailure = { e ->
//                                Toast.makeText(
//                                    requireContext(),
//                                    "Error: ${e.message}",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                Log.e("UpdateProduct", "Error updating product: ${e.message}", e)
//                            }
//                        )
//                    }
//                    ProgressDialogUtils.hideLoadingDialog()
//
//                    Toast.makeText(
//                        requireContext(),
//                        "Product updated successfully",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    findNavController().popBackStack()
//                } catch (e: Exception) {
//                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT)
//                        .show()
//                    Log.e("UpdateProduct", "Error updating product: ${e.message}", e)
//                }
//            }
//        }
//    }
    private fun setupSaveButton() {
        binding.saveProductBtn.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    ProgressDialogUtils.showLoadingDialog(requireContext())
                    val productName = binding.productNameEt.text.toString()
                    val shortDescription = binding.shortDescriptionEt.text.toString()
                    val description = binding.descriptionEt.text.toString()
                    val price = binding.priceEt.text.toString().toDouble()
                    val inventory = binding.quantityEt.text.toString().toInt()
                    val type = binding.typeSpinner.text.toString().let { PlantType.valueOf(it) }
                    val suitEnvironment = binding.suitEnvironmentSpinner.text.toString()
                        .let { SuitEnvironment.valueOf(it) }
                    val suitClimate =
                        binding.suitClimateSpinner.text.toString().let { SuitClimate.valueOf(it) }
                    val saleOff = binding.saleOffEt.text.toString().toDouble()

                    // Create updated product with all fields of the args.currentProduct
                    var updatedProduct = args.currentProduct.copy()

                    if (imageUris.isNotEmpty() && imageUris != args.currentProduct.imageUrl.map {
                            Uri.parse(
                                it
                            )
                        }) {
                        val imageUrls = withContext(Dispatchers.IO) {
                            productRepository.uploadProductImages(imageUris)
                        }
                        updatedProduct = updatedProduct.copy(
                            storeId = args.currentProduct.storeId,
                            imageUrl = imageUrls,
                            productName = productName,
                            shortDescription = shortDescription,
                            description = description,
                            price = price,
                            inventory = inventory,
                            type = type,
                            suitEnvironment = suitEnvironment,
                            suitClimate = suitClimate,
                            saleOff = saleOff
                        )
                    } else {
                        updatedProduct = updatedProduct.copy(
                            productName = productName,
                            shortDescription = shortDescription,
                            description = description,
                            price = price,
                            inventory = inventory,
                            type = type,
                            suitEnvironment = suitEnvironment,
                            suitClimate = suitClimate,
                            saleOff = saleOff
                        )
                    }

                    withContext(Dispatchers.IO) {
                        productRepository.updateProduct(updatedProduct,
                            onSuccess = {
                                Log.d("UpdateProduct", "Product updated successfully")
                            },
                            onFailure = { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Error: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("UpdateProduct", "Error updating product: ${e.message}", e)
                            }
                        )
                    }

                    ProgressDialogUtils.hideLoadingDialog()
                    Toast.makeText(
                        requireContext(),
                        "Product updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                } catch (e: Exception) {
                    ProgressDialogUtils.hideLoadingDialog()
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                    Log.e("UpdateProduct", "Error updating product: ${e.message}", e)
                }
            }
        }
    }


    private fun uploadImages(imageUris: List<Uri>): List<String> {
        val imageUrls = mutableListOf<String>()
        for (imageUri in imageUris) {
            val imageUrl = productRepository.uploadProductImage(imageUri,
                onSuccess = { url ->
                    imageUrls.add(url)
                },
                onFailure = { e ->
                    Log.e("UpdateProduct", "Error uploading image: ${e.message}", e)
                }
            )
        }
        return imageUrls
    }

    private fun setupViews() {
        val product = args.currentProduct
        binding.productNameEt.setText(product.productName)
        binding.shortDescriptionEt.setText(product.shortDescription)
        binding.descriptionEt.setText(product.description)
        binding.priceEt.setText(product.price.toString())
        binding.quantityEt.setText(product.inventory.toString())
        binding.saleOffEt.setText(product.saleOff.toString())
        binding.typeSpinner.setText(product.type.toString(), false)
        binding.suitEnvironmentSpinner.setText(product.suitEnvironment.toString(), false)
        binding.suitClimateSpinner.setText(product.suitClimate.toString(), false)

        val imageUris = product.imageUrl
        val imageAdapter = ProductImageAdapterString(imageUris)
        binding.productImageViewPager.adapter = imageAdapter

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                imageUris.add(imageUri)
                updateImageViewPager()
            }
        }
    }

    private fun updateImageViewPager() {
        if (imageUris.isNotEmpty()) {
            imageAdapter = ProductImageAdapterUri(imageUris)
            binding.productImageViewPager.adapter = imageAdapter
        }
    }

    private fun onBackPressed() {
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
