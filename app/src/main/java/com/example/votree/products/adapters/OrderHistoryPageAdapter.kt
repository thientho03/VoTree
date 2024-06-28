package com.example.votree.products.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.votree.products.fragments.AllOrdersFragment
import com.example.votree.products.fragments.DeliveredOrdersFragment
import com.example.votree.products.fragments.DeniedOrdersFragment
import com.example.votree.products.fragments.PendingOrdersFragment
import com.example.votree.products.view_models.OrderHistoryViewModel

class OrderHistoryPagerAdapter(
    fragment: FragmentActivity,
    private val viewModel: OrderHistoryViewModel
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AllOrdersFragment(viewModel)
            1 -> PendingOrdersFragment(viewModel)
            2 -> DeliveredOrdersFragment(viewModel)
            3 -> DeniedOrdersFragment(viewModel)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}