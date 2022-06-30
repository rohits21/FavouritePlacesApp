package com.example.favouriteplaces.database

import androidx.room.*
import com.example.favouriteplaces.models.FavouritePlacesModel
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouritePlacesDAO {

    @Insert
    suspend fun insert(favouritePlaceEntity: FavouritePlacesModel )

    @Delete
    suspend fun delete(favouritePlaceEntity: FavouritePlacesModel)

    @Update
    suspend fun update(favouritePlaceEntity: FavouritePlacesModel)

//    @Query("SELECT * FROM `FavouritePlacesTable`")
//    fun fetchAllPlaces():List<FavouritePlacesModel>

    @Query("SELECT * FROM `FavouritePlacesTable`")
    fun fetchAllPlaces():Flow<List<FavouritePlacesModel>>

    @Query("SELECT * FROM `FavouritePlacesTable` where id= :id")
    fun fetchPlaceById(id:Int):Flow<FavouritePlacesModel>


}