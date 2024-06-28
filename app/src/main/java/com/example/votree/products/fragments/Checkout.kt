package com.example.votree.products.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.votree.R
import com.example.votree.databinding.FragmentCheckoutBinding
import com.example.votree.products.activities.CheckoutActivity
import com.example.votree.products.adapters.CheckoutProductAdapter
import com.example.votree.products.models.Cart
import com.example.votree.products.models.ShippingAddress
import com.example.votree.products.repositories.PointTransactionRepository
import com.example.votree.products.view_models.CartViewModel
import com.example.votree.products.view_models.ProductViewModel
import com.example.votree.products.view_models.ShippingAddressViewModel
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.launch

const val DELIVERY_FEE = 10.0


class Checkout : Fragment() {
    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private lateinit var shippingAddressViewModel: ShippingAddressViewModel
    private lateinit var productViewModel: ProductViewModel
    private lateinit var cartViewModel: CartViewModel
    private var cart = Cart()
    private var shippingAddress: ShippingAddress? = null

    private var newAccumulatedPoints = 0
    private var earnPoints = 0
    private var skipPayment = true
    private val args: CheckoutArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        shippingAddressViewModel =
            ViewModelProvider(requireActivity()).get(ShippingAddressViewModel::class.java)
        productViewModel = ViewModelProvider(requireActivity()).get(ProductViewModel::class.java)
        cartViewModel = ViewModelProvider(requireActivity()).get(CartViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCheckout()
        setupAccumulatePoints()
        setupObservers()
        setupRecyclerView()
    }

    private fun setupObservers() {
        val placeOrderButton: Button = binding.placeOrderBtn
        val payBeforeDeliverySwitch: MaterialSwitch = binding.paidBeforeDeliverySw
        payBeforeDeliverySwitch.setOnCheckedChangeListener { _, isChecked ->
            skipPayment = !isChecked
        }

        placeOrderButton.setOnClickListener {
            val intent = Intent(activity, CheckoutActivity::class.java)
            intent.putExtra("totalAmount", binding.totalAmountTv.text.toString())
            intent.putExtra("cart", cart)
            intent.putExtra("receiver", shippingAddress)
            intent.putExtra("skipPayment", skipPayment)
            startActivityForResult(intent, 1)
        }

        // Observe the shippingAddress LiveData
        shippingAddressViewModel.shippingAddress.observe(viewLifecycleOwner) { address ->
            if (address != null) {
                shippingAddress = address
                updateAddressUI(address)
            }
        }

        binding.addressView.setOnClickListener {
            val action = CheckoutDirections.actionCheckoutToShippingAddressFragment()
            findNavController().navigate(action)
        }
    }

    private fun setupCheckout() {
        if (args.cart != null) {
            cart = args.cart?.copy() ?: Cart()

            Log.d("Checkout", "cart: $cart")
            cartViewModel.calculateTotalProductsPrice(cart)
                .observe(viewLifecycleOwner) { totalPrice ->
                    earnPoints = totalPrice.toInt()
                    val totalAmount = totalPrice + DELIVERY_FEE

                    binding.totalProductsPriceTv.text = getString(R.string.price_format, totalPrice)
                    binding.totalAmountTv.text = totalAmount.toString()
                    binding.deliveryFeeTv.text = getString(R.string.price_format, DELIVERY_FEE)
                    binding.totalAmountBottomTv.text = getString(R.string.price_format, totalAmount)
                }
        }

        lifecycleScope.launch {
            val pointTransactionRepository = PointTransactionRepository()
            val currentPoints = pointTransactionRepository.getCurrentPoints() ?: 0
            binding.accumulatePointsTv.text = getString(R.string.price_format, currentPoints * 0.01)
        }
    }

    private fun setupAccumulatePoints() {
        val accumulatePointsSwitch: MaterialSwitch = binding.usePointsSw
        accumulatePointsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                updateTotalAmountWithPoints()
            } else {
                updateTotalAmountWithoutPoints()
            }
        }
    }

    private fun updateTotalAmountWithPoints() {
        lifecycleScope.launch {
            val pointTransactionRepository = PointTransactionRepository()
            val currentPoints = pointTransactionRepository.getCurrentPoints() ?: 0
            val totalAmount = binding.totalAmountTv.text.toString().toDouble()

            // Calculate the discount from points (assuming 1 point = $0.01)
            val discountFromPoints = minOf(currentPoints * 0.01, totalAmount)

            // Update the UI to reflect the discount
            updateDiscountUI(discountFromPoints)
            updateTotalAmountUI(totalAmount - discountFromPoints)

            if (currentPoints * 0.01 < totalAmount) {
                newAccumulatedPoints = currentPoints
            } else {
                newAccumulatedPoints = (currentPoints * 0.01 - discountFromPoints).toInt()
                Log.d("Checkout", "newAccumulatedPoints: $newAccumulatedPoints")
            }
        }
    }

    private fun updateTotalAmountWithoutPoints() {
        cartViewModel.calculateTotalProductsPrice(cart)
            .observe(viewLifecycleOwner) { totalPrice ->
                val totalAmount = totalPrice + DELIVERY_FEE
                updateTotalAmountUI(totalAmount)
                binding.saleByPointsTv.text = getString(R.string.price_format, -0.0)
                newAccumulatedPoints = totalAmount.toInt()
            }
    }

    private fun updateDiscountUI(discountFromPoints: Double) {
        binding.saleByPointsTv.text = getString(R.string.price_format, -discountFromPoints)
    }

    private fun updateTotalAmountUI(totalAmount: Double) {
        binding.totalAmountTv.text = getString(R.string.price_format, totalAmount)
        binding.totalAmountBottomTv.text = getString(R.string.price_format, totalAmount)
    }

    private fun setupRecyclerView() {
        binding.productsRv.layoutManager = LinearLayoutManager(context)
        binding.productsRv.adapter =
            CheckoutProductAdapter(requireContext(), cart, productViewModel)
    }

    private fun updateAddressUI(address: ShippingAddress) {
        binding.userNameTv.text = address.recipientName
        binding.userPhoneNumberTv.text = address.recipientPhoneNumber
        binding.userAddressTv.text = address.recipientAddress
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("Checkout", "onActivityResult: requestCode: $requestCode, resultCode: $resultCode")
        Log.d("Checkout", "earnPoint: $earnPoints, newAccumulatedPoints: $newAccumulatedPoints")
        if (requestCode == 1 && resultCode == -1) {
            lifecycleScope.launch {
                val pointTransactionRepository = PointTransactionRepository()
                pointTransactionRepository.redeemPoints(
                    newAccumulatedPoints,
                    "Redeem points for purchase"
                )
            }
            val action = CheckoutDirections.actionCheckoutToCheckoutResultFragment(
                true,
                earnPoints,
                null
            )
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        skipPayment = false
        _binding = null
    }
}