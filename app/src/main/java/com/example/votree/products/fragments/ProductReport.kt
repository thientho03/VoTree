package com.example.votree.products.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.votree.databinding.FragmentProductReportBinding
import com.example.votree.tips.models.GeneralReport
import com.example.votree.utils.AuthHandler
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProductReport : Fragment() {
    lateinit var binding: FragmentProductReportBinding
    val args : ProductReportArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProductReportBinding.inflate(inflater, container, false)
        setupInputs()
        return binding.root
    }

    private fun setupInputs(){
        var imageUri : Uri? = null
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                Toast.makeText(activity, "Selected 1 image", Toast.LENGTH_SHORT).show()
                imageUri = uri
            } else {
                Log.d("PhotoPicker", "No media selected")
                Toast.makeText(activity, "No media selected", Toast.LENGTH_SHORT).show()
            }
        }
        binding.productReportToolbar.setNavigationOnClickListener {
            navigateUp()
        }
        binding.productReportAddImageBtn.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.productReportCancelBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.productReportSubmitBtn.setOnClickListener {
            val productId = args.productId
            val productReport = GeneralReport(
                content = binding.productReportContentEditText.text.toString(),
                shortDescription = binding.productReportReason.text.toString(),
                productId = productId,
                reporterId = AuthHandler.firebaseAuth.currentUser?.uid ?: "",
            )
            pushReportToDatabase(productReport, imageUri)
        }
    }

    private fun pushReportToDatabase(productReport: GeneralReport, imageUri: Uri?){
        val fireStoreInstance = FirebaseFirestore.getInstance()
        val storageInstance = FirebaseStorage.getInstance()

        val storageRef = storageInstance.reference.child("images/reports/${imageUri?.lastPathSegment}")
        if (imageUri !== null){
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener {
                        productReport.imageList[0]  = it.toString()
                        fireStoreInstance.collection("reports").add(productReport)
                            .addOnSuccessListener { documentReference ->
                                val documentId = documentReference.id
                                fireStoreInstance.collection("reports").document(documentId)
                                    .update("id", documentId)
                                Toast.makeText(
                                    activity,
                                    "Thank you for your contribution",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navigateUp()
                            }
                            .addOnFailureListener {
                                Toast.makeText(activity, "Product report Error: ${it.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Image upload Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
        else{
            fireStoreInstance.collection("reports").add(productReport)
                .addOnSuccessListener { documentReference ->
                    val documentId = documentReference.id
                    fireStoreInstance.collection("reports").document(documentId)
                        .update("id", documentId)
                    Toast.makeText(
                        activity,
                        "Thank you for your contribution",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateUp()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Product report Error: ${it.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }
    }

    private fun navigateUp(){
        findNavController().navigateUp()
    }
}