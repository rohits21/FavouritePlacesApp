package com.example.favouriteplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.favouriteplaces.adapter.PlaceAdapter
import com.example.favouriteplaces.database.FavouritePlacesApp
import com.example.favouriteplaces.database.FavouritePlacesDAO
import com.example.favouriteplaces.database.FavouritePlacesDatabase
import com.example.favouriteplaces.databinding.ActivityMainBinding
import com.example.favouriteplaces.models.FavouritePlacesModel
import com.example.favouriteplaces.utils.SwipeToDeleteCallback
import com.example.favouriteplaces.utils.SwipeToEditCallback
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var binding : ActivityMainBinding?= null
    private var placeDao: FavouritePlacesDAO?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
       // val employeeDao = (application as EmployeeApp).db.employeeDao()

        //placeDao = (application as FavouritePlacesApp).db.favouritePlacesDao()
        placeDao = FavouritePlacesDatabase.getInstance(this).favouritePlacesDao()
        if(placeDao!=null){
           // Toast.makeText(this@MainActivity, "Data Received", Toast.LENGTH_SHORT).show()
        }

        binding?.fabAddHappyPlace?.setOnClickListener {
            val intent = Intent(this, AddFavouritePlaceActivity::class.java)
            startActivity(intent)
            //finish()
        }
        getDataFromDB()

    }

    private fun getDataFromDB(){
        lifecycle.coroutineScope.launch {
            Toast.makeText(this@MainActivity, "New Thread launched", Toast.LENGTH_SHORT).show()
            placeDao?.fetchAllPlaces()!!.collect{
                Log.d("exactemployee", "$it")
                val list = ArrayList(it)

                Toast.makeText(this@MainActivity, "Data Received", Toast.LENGTH_SHORT).show()
                setUpDataIntoRecyclerView(list)
                Log.d("placesdata",list.toString())
            }
        }
    }
    private fun setUpDataIntoRecyclerView(placeList:ArrayList<FavouritePlacesModel>){
        if(placeList.size>0){
            binding?.rvFavouritePlaces?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
        }
        val placeAdapter = PlaceAdapter(this,placeList)
        binding?.rvFavouritePlaces?.adapter = placeAdapter
        binding?.rvFavouritePlaces?.layoutManager = LinearLayoutManager(this)

        placeAdapter.setOnClickListener(object : PlaceAdapter.OnClickListener{
            override fun onClick(position: Int, model: FavouritePlacesModel) {
                val intent = Intent(this@MainActivity,FavouritePlaceDetail::class.java)
               // Toast.makeText(this@MainActivity, "View CLicked", Toast.LENGTH_SHORT).show()
                intent.putExtra(EXTRA_PLACE_DETAILS,model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object : SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvFavouritePlaces!!.adapter as PlaceAdapter
                adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }

        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding?.rvFavouritePlaces )


        val deleteSwipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = binding?.rvFavouritePlaces!!.adapter as PlaceAdapter

                adapter.removeAtad(viewHolder.adapterPosition)
                getDataFromDB()

               // adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)

            }

        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(binding?.rvFavouritePlaces )





    }

    companion object{
         const val EXTRA_PLACE_DETAILS = "extra_place_details"
        const val  ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
    }
}