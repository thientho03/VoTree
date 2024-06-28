package com.example.votree.products.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.votree.databinding.FragmentShippingAddressBinding
import com.example.votree.products.models.ShippingAddress
import com.example.votree.products.view_models.ShippingAddressViewModel
import com.example.votree.users.activities.AddressActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ShippingAddressFragment : Fragment() {
    private lateinit var binding: FragmentShippingAddressBinding
    private lateinit var viewModel: ShippingAddressViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentShippingAddressBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(ShippingAddressViewModel::class.java)

        setupAddress()
        setupObservers()
        binding.confirmBtn.setOnClickListener {
            onSaveAddress()
        }

        return binding.root
    }

    private fun setupObservers() {
        viewModel.shippingAddress.observe(viewLifecycleOwner) { address ->
            if (address != null) {
                fillForm(address)
            }
        }
    }

    private fun setupAddress() {
        binding.recipentAddressEt.setOnClickListener {
            val intent = Intent(requireContext(), AddressActivity::class.java)
            startActivityForResult(intent, ADDRESS_REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADDRESS_REQUEST_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val address = data?.getStringExtra("address")
            binding.recipentAddressEt.setText(address)
        }
    }

    private fun fillForm(address: ShippingAddress) {
        binding.recipientNameEt.setText(address.recipientName)
        binding.recipentAddressEt.setText(address.recipientAddress)
        binding.recipientPhoneNumberEt.setText(address.recipientPhoneNumber)
    }

    private fun onSaveAddress() {
        val recipientName = binding.recipientNameEt.text.toString().trim()
        val phoneNumber = binding.recipientPhoneNumberEt.text.toString().trim()
        val addressText = binding.recipentAddressEt.text.toString().trim()
        val isDefault = binding.setAsDefaultSwitch.isChecked

        if (recipientName.isBlank() || phoneNumber.isBlank() || addressText.isBlank()) {
            Snackbar.make(binding.root, "Please fill all fields", Snackbar.LENGTH_SHORT).show()
            return
        }

        val shippingAddress = ShippingAddress(
            recipientName = recipientName,
            recipientPhoneNumber = phoneNumber,
            recipientAddress = addressText,
            default = isDefault
        )

        lifecycleScope.launch {
            viewModel.saveShippingAddress(shippingAddress)
        }

        findNavController().popBackStack()
    }

    companion object {
        const val ADDRESS_REQUEST_CODE = 1111
    }
}