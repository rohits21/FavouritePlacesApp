package com.example.favouriteplaces.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.favouriteplaces.models.FavouritePlacesModel

@Database(entities = [FavouritePlacesModel::class], version = 2)
abstract class FavouritePlacesDatabase : RoomDatabase(){

    abstract fun favouritePlacesDao():FavouritePlacesDAO

    companion object{

        @Volatile
        private var INSTANCE :FavouritePlacesDatabase ?= null

        fun getInstance(context: Context):FavouritePlacesDatabase{
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        FavouritePlacesDatabase::class.java,
                        "favourite_places_database"
                    ).fallbackToDestructiveMigration().build()
                }
                return instance
            }

        }
    }


}