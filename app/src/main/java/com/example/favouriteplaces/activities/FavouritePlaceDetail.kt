package com.example.favouriteplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.favouriteplaces.databinding.ActivityFavouritePlaceDetailBinding
import com.example.favouriteplaces.models.FavouritePlacesModel

class FavouritePlaceDetail : AppCompatActivity() {
    private var binding: ActivityFavouritePlaceDetailBinding ?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFavouritePlaceDetailBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        var favouritePlaceDetail : FavouritePlacesModel ?=null
        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            favouritePlaceDetail = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as FavouritePlacesModel
        }

        if(favouritePlaceDetail != null){

            setSupportActionBar(binding?.toolbarHappyPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = favouritePlaceDetail.title

            binding?.toolbarHappyPlaceDetail!!.setNavigationOnClickListener {
                onBackPressed()
            }

            binding?.ivPlaceImage!!.setImageURI(Uri.parse(favouritePlaceDetail.image))
            binding?.tvDescription!!.text = favouritePlaceDetail.description
            binding?.tvLocation!!.text = favouritePlaceDetail.location

            binding?.btnViewOnMap?.setOnClickListener {
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, favouritePlaceDetail)
                startActivity(intent)
            }
        }


    }
}