package com.example.votree

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.products.activities.CheckoutActivity
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

class PurchaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.upgrade_premium)

        val product1 = findViewById<LinearLayout>(R.id.one_month)
        val product2 = findViewById<LinearLayout>(R.id.six_month)
        val product3 = findViewById<LinearLayout>(R.id.one_year)
        val backBtn = findViewById<ImageButton>(R.id.back_button)

        product1.setOnClickListener {
            purchaseProduct("prod_Q2ZgygFEMIEQQq", "price_1PCUXHL1bECNnFcvaOWt26Vj")
        }

        product2.setOnClickListener {
            purchaseProduct("prod_Q2ZjppsU2YISdN", "price_1PCUaFL1bECNnFcvLh848hRr")
        }

        product3.setOnClickListener {
            purchaseProduct("prod_Q2ZkPUOhLvTpLE", "price_1PCUabL1bECNnFcvvmKt9edx")
        }

        backBtn.setOnClickListener {
            finish()
        }
    }

    private fun purchaseProduct(productId: String, priceId: String) {
        val intent = Intent(this, CheckoutActivity::class.java)
        intent.putExtra("productId", productId)
        intent.putExtra("priceId", priceId)
        startActivityForResult(intent, 1)
    }
}