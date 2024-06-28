package com.example.votree.users.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.votree.BuildConfig
import com.example.votree.R
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

class AddressActivity : AppCompatActivity() {
    // Initialize Places.
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_address)

        init()

        // Initialize the AutocompleteSupportFragment and handle the result
        val autocompleteFragment = supportFragmentManager.findFragmentById(R.id.address_acf)
                as AutocompleteSupportFragment
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS))
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val intent = Intent()
                intent.putExtra("address", place.address)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }

            override fun onError(status: Status) {
                Log.e("PlacesAPI", "An error occurred: $status")
            }
        })
    }

    private fun init(){
        val apiKey = BuildConfig.PLACES_API_KEY

        if (apiKey.isEmpty()){
            Log.e("PlacesAPI", "API key is missing")
        }else{
            Log.d("PlacesAPI", "API key is present")
            Places.initializeWithNewPlacesApiEnabled(this, apiKey)
        }
        placesClient = Places.createClient(this)
    }
}
