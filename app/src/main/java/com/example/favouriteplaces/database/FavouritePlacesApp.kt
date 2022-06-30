package com.example.favouriteplaces.database

import android.app.Application

class FavouritePlacesApp: Application() {

    val db by lazy {
        FavouritePlacesDatabase.getInstance(this)
    }
}