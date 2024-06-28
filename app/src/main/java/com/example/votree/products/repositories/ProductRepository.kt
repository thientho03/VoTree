package com.example.votree.products.repositories

import android.net.Uri
import android.util.Log
import com.example.votree.products.models.Product
import com.example.votree.products.models.ProductReview
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class ProductRepository(private val firestore: FirebaseFirestore) {
    suspend fun updateProductInventory(productId: String, quantityPurchased: Int) {
        val productRef = firestore.collection("products").document(productId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(productRef)
            val newInventory = snapshot.getLong("inventory")!! - quantityPurchased
            transaction.update(productRef, "inventory", newInventory)
        }.await()
    }

    fun fetchProducts(
        storeId: String,
        onSuccess: (List<Product>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("products")
            .whereEqualTo("storeId", storeId)
            .get()
            .addOnSuccessListener { documents ->
                val productList = documents.toObjects(Product::class.java)
                onSuccess(productList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun uploadProductImage(
        imageUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val formatter = java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
        val now = java.util.Calendar.getInstance().time
        val fileName = formatter.format(now)
        val storageRef = FirebaseStorage.getInstance().reference.child("images/products/$fileName")

        storageRef.putFile(imageUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

    private suspend fun uploadProductOneImage(imageUri: Uri): String {
        return suspendCoroutine { continuation ->
            val formatter = java.text.SimpleDateFormat("yyyyMMdd_HHmmss")
            val now = java.util.Calendar.getInstance().time
            val fileName = formatter.format(now)
            val storageRef =
                FirebaseStorage.getInstance().reference.child("images/products/$fileName")

            storageRef.putFile(imageUri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    continuation.resume(uri.toString())
                }
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }
    }

    suspend fun uploadProductImages(imageUris: List<Uri>): List<String> {
        val imageUrls = mutableListOf<String>()
        for (imageUri in imageUris) {
            val imageUrl = uploadProductOneImage(imageUri)
            imageUrls.add(imageUrl)
        }
        return imageUrls
    }


    fun addProduct(product: Product, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("products").add(product)
            .addOnSuccessListener { documentReference ->
                val productId = documentReference.id
                firestore.collection("products").document(productId).update("id", productId)
                    .addOnSuccessListener {
                        onSuccess(productId)
                    }
                    .addOnFailureListener { e ->
                        Log.e("ProductRepository", "Error updating product with ID", e)
                        onFailure(e)
                    }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun updateProduct(
        product: Product,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        product.id.takeIf { it.isNotEmpty() }?.let { productId ->
            firestore.collection("products").document(productId).set(product)
                .addOnSuccessListener {
                    onSuccess(productId)
                }
                .addOnFailureListener {
                    Log.e("ProductRepository", "Error updating product", it)
                    onFailure(it)
                }
        }
    }

    fun deleteProduct(product: Product, onComplete: (Boolean) -> Unit) {
        // First, delete all the images from Firebase Storage
        if (product.imageUrl.isNotEmpty()) {
            val deleteOperations = product.imageUrl.map { imageUrl ->
                val imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                imageRef.delete()
            }

            Tasks.whenAll(deleteOperations)
                .addOnSuccessListener {
                    // Case all images are deleted, delete the product from Firestore
                    Log.d("ProductRepository", "Images deleted")
                    firestore.collection("products").document(product.id).delete()
                        .addOnSuccessListener {
                            Log.d("ProductRepository", "Product deleted")
                            onComplete(true)
                        }
                        .addOnFailureListener {
                            Log.e("ProductRepository", "Error deleting product", it)
                            onComplete(false)
                        }
                }
                .addOnFailureListener {
                    Log.e("ProductRepository", "Error deleting images", it)
                    onComplete(false)
                }
        } else {
            // Case product doesn't have any images, delete the product from Firestore
            firestore.collection("products").document(product.id).delete()
                .addOnSuccessListener {
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        }
    }

    suspend fun getProduct(productId: String?): Product {
        val productRef = firestore.collection("products").document(productId!!)
        val snapshot = productRef.get().await()
        return snapshot.toObject(Product::class.java)!!
    }

    suspend fun updateProductSoldQuantity(productId: String, quantityPurchased: Int) {
        val productRef = firestore.collection("products").document(productId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(productRef)
            val newQuantitySold = snapshot.getLong("quantitySold")!! + quantityPurchased
            transaction.update(productRef, "quantitySold", newQuantitySold)
        }.await()
    }

    suspend fun getAverageProductRating(productId: String): Float {
        val reviewsSnapshot =
            firestore.collection("products").document(productId).collection("reviews").get().await()
        val reviews = reviewsSnapshot.toObjects(ProductReview::class.java)
        return if (reviews.isNotEmpty()) {
            reviews.map { it.rating }.average().toFloat()
        } else {
            0.0f
        }
    }

    fun toggleProductVisibility(
        product: Product,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val newVisibility = !product.active
        firestore.collection("products").document(product.id).update("active", newVisibility)
            .addOnSuccessListener {
                onSuccess(product.id)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
}