package com.example.votree.tips

import android.content.Context
import android.view.View
import com.example.votree.utils.FirebaseRealtime
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView

object AdManager {
    private var isPremium = false
    private val adViews = mutableListOf<AdView>()

    fun addAdView(adView: AdView, context: Context) {
        adViews.add(adView)
        loadAd(adView, context)
    }

    private fun loadAd(adView: AdView, context: Context) {
        if (!isPremium) {
            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } else {
            adView.visibility = View.GONE
        }
    }

    fun setPremium(premium: Boolean, context: Context) {
        isPremium = premium
        adViews.forEach { adView ->
            if (premium) {
                adView.visibility = View.GONE
            } else {
                loadAd(adView, context)
            }
        }
    }

    fun setPremiumOnFirebase(premium: Boolean) {
        FirebaseRealtime.getInstance().setPremiumOnFirebase(premium)
    }

    fun removeAdView(adView: AdView) {
        adViews.remove(adView)
    }
}