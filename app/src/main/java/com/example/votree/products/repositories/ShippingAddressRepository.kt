package com.example.votree.products.repositories

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.votree.products.models.ShippingAddress
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ShippingAddressRepository @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val userId get() = auth.currentUser?.uid!!
    private val usersCollection = db.collection("users")
    private val addressesCollection =
        db.collection("users").document(userId).collection("addresses")

    suspend fun saveShippingAddress(address: ShippingAddress, userId: String) {
        try {
            // Check for duplicate addresses
            if (isDuplicateAddress(address)) {
                Log.d("ShippingAddressRepo", "Duplicate address found. Aborting save.")
                return
            }

            // If the new address is set as the default, update all other default addresses to non-default
            updateOtherDefaultAddresses(address)

            // Save the new address or update the existing one
            val savedAddress = saveOrUpdateAddress(address)
            Log.d("ShippingAddressRepo", "Saved/updated address with ID: ${savedAddress.id}")

            // Update the user's document with the new default address ID if the address is set as default
            if (address.default) {
                updateUserDefaultAddress(userId, savedAddress.id)
                Log.d(
                    "ShippingAddressRepo",
                    "Updated user $userId default address to ${savedAddress.id}"
                )
            }
        } catch (e: Exception) {
            Log.e("ShippingAddressRepo", "Error saving/updating address: ${e.message}", e)
            throw e
        }
    }

    private suspend fun isDuplicateAddress(address: ShippingAddress): Boolean {
        val duplicateQuery = addressesCollection
            .whereEqualTo("recipientName", address.recipientName)
            .whereEqualTo("recipientAddress", address.recipientAddress)
            .whereEqualTo("recipientPhoneNumber", address.recipientPhoneNumber)
            .get().await()

        return duplicateQuery.documents.isNotEmpty()
    }

    private suspend fun updateOtherDefaultAddresses(newDefaultAddress: ShippingAddress) {
        if (newDefaultAddress.default) {
            val defaultAddresses =
                addressesCollection.whereEqualTo("default", true).get().await().documents
            for (doc in defaultAddresses) {
                doc.reference.update("default", false).await()
                Log.d("ShippingAddressRepo", "Updated address ${doc.id} to not be the default.")
            }
        }
    }

    private suspend fun saveOrUpdateAddress(address: ShippingAddress): ShippingAddress {
        return if (address.id.isNullOrEmpty()) {
            val newDocRef = addressesCollection.document()
            address.id = newDocRef.id
            newDocRef.set(address).await()
            address
        } else {
            addressesCollection.document(address.id).set(address).await()
            address
        }
    }

    private suspend fun updateUserDefaultAddress(userId: String, defaultAddressId: String) {
        val userDocRef = usersCollection.document(userId)
        userDocRef.update("address", defaultAddressId).await()
    }

    suspend fun getShippingAddresses(
        shippingAddress: MutableLiveData<List<ShippingAddress>?>,
        selectedShippingAddress: MutableLiveData<ShippingAddress?>
    ): List<ShippingAddress> {
        val addresses = addressesCollection.get().await()
            .documents.mapNotNull { it.toObject(ShippingAddress::class.java) }
        Log.d("ShippingAddressRepo", "Fetched shipping addresses: $addresses")
        selectedShippingAddress.postValue(addresses.find { it.default })
        selectedShippingAddress.value = addresses.find { it.default }
        return addresses
    }

    suspend fun fetchAddresses(): List<ShippingAddress> {
        return addressesCollection.get()
            .await().documents.mapNotNull { it.toObject(ShippingAddress::class.java) }
    }

    suspend fun getDefaultShippingAddress(): ShippingAddress? {
        return addressesCollection.whereEqualTo("default", true).get().await()
            .documents.firstOrNull()?.toObject(ShippingAddress::class.java)
    }
}
