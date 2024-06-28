package com.example.votree.models

import android.os.Parcelable
import com.example.votree.products.data.productCatagories.PlantType
import com.example.votree.products.data.productCatagories.SuitClimate
import com.example.votree.products.data.productCatagories.SuitEnvironment
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Product(
    var id: String = "", // Change id to String if you're using Firestore's auto-generated document ID
    val storeId: String = "",
    var imageUrl: List<String> = mutableListOf(),
    val productName: String = "",
    val shortDescription: String = "",
    val description: String = "",
    val averageRate: Double = 0.0,
    val quantityOfRate: Int = 0,
    val price: Double = 0.0,
    val inventory: Int = 0,
    val quantitySold: Int = 0,
    val type: PlantType = PlantType.UNKNOWN,
    val suitEnvironment: SuitEnvironment = SuitEnvironment.UNKNOWN,
    val suitClimate: SuitClimate = SuitClimate.UNKNOWN,
    val saleOff: Double = 0.0,
    val createdAt: Date = Date(),
    var updatedAt: Date = Date(),
    var currentQuantitySold: Int = 0,
    @field:JvmField
    val isActive: Boolean = true
) : Parcelable {
    override fun toString(): String {
        return "Product(id=$id, name='$productName', price=$price)"
    }
}