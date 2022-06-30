package com.example.favouriteplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.example.favouriteplaces.R
import com.example.favouriteplaces.databinding.ActivityMapBinding
import com.example.favouriteplaces.models.FavouritePlacesModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapActivity : AppCompatActivity() ,OnMapReadyCallback{
    private var binding : ActivityMapBinding ?= null

    private var favouritePlaceModel : FavouritePlacesModel ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
           favouritePlaceModel =  intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as FavouritePlacesModel
        }

        if(favouritePlaceModel !=null){
            setSupportActionBar(binding?.toolbarMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = favouritePlaceModel!!.title

            binding?.toolbarMap!!.setNavigationOnClickListener {
                onBackPressed()
            }

            val supportMapFragment : SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

            lifecycleScope.launch {
                supportMapFragment.getMapAsync(this@MapActivity)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val position = LatLng(favouritePlaceModel!!.latitude,favouritePlaceModel!!.longitude)
        googleMap!!.addMarker(MarkerOptions().position(position).title(favouritePlaceModel!!.location))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position,15f)
        googleMap.animateCamera(newLatLngZoom)


    }
}