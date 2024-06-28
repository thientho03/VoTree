package com.example.votree.utils

sealed class CartItemUpdateResult {
    object Success : CartItemUpdateResult()
    object InventoryExceeded : CartItemUpdateResult()
    object MinimumQuantityReached : CartItemUpdateResult()
    object InventoryUnavailable : CartItemUpdateResult()
}