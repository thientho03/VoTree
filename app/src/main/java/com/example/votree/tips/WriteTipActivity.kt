package com.example.votree.tips

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.votree.R
import com.example.votree.databinding.ActivityWriteTipBinding
import com.example.votree.tips.models.ProductTip
import com.example.votree.utils.AuthHandler
import com.google.android.material.button.MaterialButton
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class WriteTipActivity : AppCompatActivity(R.layout.activity_write_tip) {
    private val fireStoreInstance = FirebaseFirestore.getInstance()
    private val storageInstance = FirebaseStorage.getInstance()
    var imageUri : Uri? = null
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityWriteTipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: $uri")
                Toast.makeText(this, "Selected 1 image", Toast.LENGTH_SHORT).show()
                binding.writeTipPreviewIv.setImageURI(uri)
                imageUri = uri
            } else {
                Log.d("PhotoPicker", "No media selected")
                Toast.makeText(this, "No media selected", Toast.LENGTH_SHORT).show()
            }
        }

        // Add thumbnail button
        findViewById<MaterialButton>(R.id.add_thumbnail_btn).setOnClickListener{
            Log.d("PhotoPicker", "Launching photo picker")
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.submitNewTipButton.setOnClickListener{
            val tip = ProductTip(
                0,
                content=binding.tipContentInputEditText.text.toString(),
                shortDescription=binding.tipShortDescriptionInputEditText.text.toString().replace("\\n", System.getProperty("line.separator") ?: "\n"),
                title=binding.tipTitleInputEditText.text.toString(),
                userId = AuthHandler.firebaseAuth.currentUser?.uid ?: "",
                vote_count=0)
            pushTiptoDatabase(tip)
        }
        binding.cancelNewTipButton.setOnClickListener{
            finish()
        }
    }

    private fun pushTiptoDatabase(tip: ProductTip) {
        if (!checkTipContent(tip)) {
            return
        }
        val storageRef = storageInstance.reference.child("images/productTips/${imageUri?.lastPathSegment}")
        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    tip.imageList[0] = uri.toString()
                    fireStoreInstance.collection("ProductTip").add(tip)
                        .addOnSuccessListener { documentReference ->
                            val documentId = documentReference.id
                            lifecycleScope.launch {
                                addFirestoreDocument("checkContent", "Please check this content (Harassment, Hate speech, Sexually explicit content, Dangerous content, not Plant-related, meaningless content and not a tip for plant should be rejected): " + "${tip.title} - ${tip.shortDescription} - ${tip.content}", documentId)
                            }
                            fireStoreInstance.collection("ProductTip").document(documentId)
                                .update("id", documentId)
                            Toast.makeText(
                                this,
                                "Tip sent successfully, please wait for approval",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkTipContent(tip: ProductTip): Boolean {
        if (tip.title.isEmpty() || tip.content.isEmpty() || tip.shortDescription.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        if (imageUri == null) {
            Toast.makeText(this, "Please add an image", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    suspend fun addFirestoreDocument(collectionName: String, tipContent: String, tipId: String) {
        val documentData = hashMapOf(
            "tipContent" to tipContent,
            "tipId" to tipId
        )

        // Add the document to Firestore
        try {
            withContext(Dispatchers.IO) {
                db.collection(collectionName).add(documentData).await()
            }
            // Document added successfully
        } catch (e: Exception) {
            // Handle any errors
            println("Error adding document: $e")
        }
    }
}
