package com.example.votree.tips

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.votree.R
import com.example.votree.databinding.ActivityTipReportBinding
import com.example.votree.tips.models.ProductTip
import com.example.votree.tips.models.GeneralReport
import com.example.votree.utils.AuthHandler
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class TipReportActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityTipReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupInputs(binding)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupInputs(binding: ActivityTipReportBinding){
        var imageUri : Uri? = null
        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                Toast.makeText(this, "Selected 1 image", Toast.LENGTH_SHORT).show()
                imageUri = uri
            } else {
                Log.d("PhotoPicker", "No media selected")
                Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tipReportToolbar.setNavigationOnClickListener {
            finish()
        }
        binding.tipReportAddImageBtn.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.tipReportCancelBtn.setOnClickListener {
            finish()
        }

        binding.tipReportSubmitBtn.setOnClickListener {
            val tipData = getTipData() ?: return@setOnClickListener
            val generalReport = GeneralReport(
                content = binding.tipReportContentEditText.text.toString(),
                shortDescription = binding.tipReportReason.text.toString(),
                reporterId = AuthHandler.firebaseAuth.currentUser?.uid ?: "",
                tipId = tipData.id
            )
            pushReportToDatabase(generalReport, imageUri)
        }
    }

    private fun pushReportToDatabase(generalReport: GeneralReport, imageUri: Uri?){
        val fireStoreInstance = FirebaseFirestore.getInstance()
        val storageInstance = FirebaseStorage.getInstance()

        val storageRef = storageInstance.reference.child("images/reports/${imageUri?.lastPathSegment}")
        if (imageUri !== null){
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener {
                        generalReport.imageList[0]  = it.toString()
                        fireStoreInstance.collection("reports").add(generalReport)
                            .addOnSuccessListener { documentReference ->
                                val documentId = documentReference.id
                                fireStoreInstance.collection("reports").document(documentId)
                                    .update("id", documentId)
                                Toast.makeText(
                                    this,
                                    "Thank you for your contribution",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Tip report Error: ${it.message}", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Image upload Error: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
        else{
            fireStoreInstance.collection("reports").add(generalReport)
                .addOnSuccessListener { documentReference ->
                    val documentId = documentReference.id
                    fireStoreInstance.collection("reports").document(documentId)
                        .update("id", documentId)
                    Toast.makeText(
                        this,
                        "Thank you for your contribution",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Tip report Error: ${it.message}", Toast.LENGTH_SHORT)
                        .show()
                }
        }

    }

    private fun getTipData() : ProductTip?{
        val tipData = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra("tipData", ProductTip::class.java)
        else
            intent.getParcelableExtra("tipData")

        return tipData
    }
}