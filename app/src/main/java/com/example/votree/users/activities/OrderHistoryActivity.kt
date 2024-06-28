package com.example.votree.users.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.votree.R
import com.example.votree.databinding.ActivityOrderHistoryBinding
import com.example.votree.products.adapters.OrderHistoryPagerAdapter
import com.example.votree.products.view_models.OrderHistoryViewModel
import com.example.votree.tips.AdManager
import com.google.android.gms.ads.AdView
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class OrderHistoryActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var binding: ActivityOrderHistoryBinding
    private val viewModel: OrderHistoryViewModel by viewModels()
    private lateinit var job: Job

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        job = Job()

        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pagerAdapter = OrderHistoryPagerAdapter(this, viewModel)
        binding.orderHistoryVp.adapter = pagerAdapter

        TabLayoutMediator(binding.sortOrderTl, binding.orderHistoryVp) { tab, position ->
            tab.text = when (position) {
                0 -> "All"
                1 -> "Pending"
                2 -> "Delivered"
                3 -> "Denied"
                else -> throw IllegalArgumentException("Invalid position: $position")
            }
        }.attach()

        // Set the navigation icon to the back button
        binding.toolbar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.arrow_back_24px)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.toolbar.title = getString(R.string.order_history)

        val adView = findViewById<AdView>(R.id.adView)
        AdManager.addAdView(adView, this)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
