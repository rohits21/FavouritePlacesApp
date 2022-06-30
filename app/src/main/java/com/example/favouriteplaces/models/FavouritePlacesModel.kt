package com.example.favouriteplaces.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable


@Entity(tableName = "FavouritePlacesTable")
data class FavouritePlacesModel(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String = "",
    val image: String = "",
    val description : String = "",
    val date : String = "",
    val location : String = "",
    val latitude : Double = 23.2599,
    val longitude : Double = 77.4126

):Serializable
