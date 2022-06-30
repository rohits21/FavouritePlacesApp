package com.example.favouriteplaces.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.widget.Toast
import com.example.favouriteplaces.databinding.ActivityAddFavouritePlaceBinding
import com.karumi.dexter.Dexter
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.example.favouriteplaces.R
import com.example.favouriteplaces.database.FavouritePlacesApp
import com.example.favouriteplaces.database.FavouritePlacesDAO
import com.example.favouriteplaces.database.FavouritePlacesDatabase
import com.example.favouriteplaces.models.FavouritePlacesModel
import com.example.favouriteplaces.utils.GetAddressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddFavouritePlaceActivity : AppCompatActivity() {
    private val cal = Calendar.getInstance()
    private lateinit var dateSetListener : DatePickerDialog.OnDateSetListener
    private var saveImg : Uri ?= null
    private var mLatitude : Double = 0.0
    private var mLongitude: Double = 0.0
    //lateinit var placeDao: FavouritePlacesDAO
    private var mHappyPlace : FavouritePlacesModel ?= null
    private lateinit var mFusedLocationClient : FusedLocationProviderClient

    private var binding : ActivityAddFavouritePlaceBinding ?= null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddFavouritePlaceBinding.inflate(layoutInflater)
        setContentView(binding?.root)

      //  val employeeDao = (application as EmployeeApp).db.employeeDao()

        // val placeDao = (application as FavouritePlacesApp).db.favouritePlacesDao()
         val placeDao = FavouritePlacesDatabase.getInstance(this).favouritePlacesDao()

        setSupportActionBar(binding?.toolbarAddPlace)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding?.toolbarAddPlace?.setNavigationOnClickListener {
            onBackPressed()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if(!Places.isInitialized()){

            Places.initialize(this@AddFavouritePlaceActivity, resources.getString(R.string.api_key))
        }

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlace = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS) as FavouritePlacesModel
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->

            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()

        }
        updateDateInView()

        if(mHappyPlace != null){
            updateDetails()
        }

        binding?.etDate?.setOnClickListener {
            DatePickerDialog(this,dateSetListener,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding?.tvAddImage?.setOnClickListener {
            val pictureDialog = AlertDialog.Builder(this)
            pictureDialog.setTitle("Select Action")
            val pictureDialogItems = arrayOf("Select photo from gallery" , "Capture picture from camera")
            pictureDialog.setItems(pictureDialogItems){dialog, which->
                when(which){
                    0-> choosePhotoFromGallery()

                    1-> takePhotoFromCamera()
                }

            }.show()
        }

        binding?.btnSave?.setOnClickListener {
            when{
                binding?.etTitle?.text.toString().isNullOrEmpty() ->{
                    Toast.makeText(this, "Title is empty", Toast.LENGTH_SHORT).show()

                }
                binding?.etDescription?.text.toString().isNullOrEmpty() ->{
                    Toast.makeText(this, "Description is empty", Toast.LENGTH_SHORT).show()
                }
                binding?.etLocation?.text.toString().isNullOrEmpty() ->{
                    binding?.etLocation?.setText("abc")
                    Toast.makeText(this, "Location is empty", Toast.LENGTH_SHORT).show()
                }
                saveImg == null -> {
                    Toast.makeText(this, "Please select an Image", Toast.LENGTH_SHORT).show()
                }
                else->{

                    addRecordToDatabase(placeDao)
                }
            }
        }

        binding?.tvSelectCurrentLocation?.setOnClickListener {
            setCurrentLocation()
        }

        binding?.etLocation?.setOnClickListener {
            try{

                val fields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG,
                    Place.Field.ADDRESS
                )
                // Start the autocomplete intent with a unique request code.
                val intent =
                    Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .build(this@AddFavouritePlaceActivity)
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
            }catch(e:Exception){
                e.printStackTrace()
            }
        }

    }

    private fun isLocationEnabled(): Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            mLatitude = mLastLocation.latitude
            Log.e("Current Latitude", "$mLatitude")
            mLongitude = mLastLocation.longitude
            Log.e("Current Longitude", "$mLongitude")

            // TODO(Step 2: Call the AsyncTask class fot getting an address from the latitude and longitude.)
            // START
            val addressTask =
                GetAddressFromLatLng(this@AddFavouritePlaceActivity, mLatitude, mLongitude)

            addressTask.setAddressListener(object :
                GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    Log.e("Address ::", "" + address)
                    binding?.etLocation!!.setText(address) // Address is set to the edittext
                }

                override fun onError() {
                    Log.e("Get Address ::", "Something is wrong...")
                }
            })

            addressTask.getAddress()
            // END
        }
    }

    private fun setCurrentLocation(){

        if(!isLocationEnabled()){
            Toast.makeText(this, "Your location is turned off", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }

        else{
            Dexter.withContext(this).withPermissions(

                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ).withListener(object : MultiplePermissionsListener{
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                    if(report!!.areAllPermissionsGranted()){

                        Toast.makeText(this@AddFavouritePlaceActivity, "Permission granted", Toast.LENGTH_SHORT).show()
                        requestNewLocationData()
                    }

                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationaleDialogForPermissions()
                }
            }).onSameThread().check()
        }



    }

    private fun updateDetails(){
        supportActionBar?.title = "Edit Favourite Place"
        binding?.etTitle?.setText(mHappyPlace?.title)
        binding?.etDescription?.setText(mHappyPlace?.description)
        binding?.etLocation?.setText(mHappyPlace?.location)
        binding?.etDate?.setText(mHappyPlace?.date)
        mLongitude = mHappyPlace!!.longitude
        mLatitude = mHappyPlace!!.latitude
        saveImg = Uri.parse(mHappyPlace!!.image)

        binding?.ivPlaceImage!!.setImageURI(saveImg)
        binding?.btnSave?.text = "UPDATE"

    }

    private fun addRecordToDatabase(placeDao : FavouritePlacesDAO){

        val model = FavouritePlacesModel(if(mHappyPlace == null) 0 else mHappyPlace!!.id,
            binding?.etTitle?.text.toString(),
            saveImg.toString(),
            binding?.etDescription?.text.toString(),
            binding?.etDate?.text.toString(),
            binding?.etLocation?.text.toString(),
            mLatitude,
            mLongitude

            )
        lifecycleScope.launch {
            if(mHappyPlace != null){
                Toast.makeText(this@AddFavouritePlaceActivity, "Updated", Toast.LENGTH_SHORT).show()
                placeDao.update(model)

            }else{
                Toast.makeText(this@AddFavouritePlaceActivity, "Record Saved", Toast.LENGTH_SHORT).show()
                placeDao.insert(model)
            }


            startActivity(Intent(this@AddFavouritePlaceActivity, MainActivity::class.java))
            finish()
        }



    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){

            if(requestCode == GALLERY){

                if(data != null){
                    val contentUri = data.data
                    try{
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentUri)
                        saveImg = saveImageToInternalStorage(selectedImageBitmap)
                        binding?.ivPlaceImage!!.setImageBitmap(selectedImageBitmap)
                    }
                    catch(e:IOException){
                        e.printStackTrace()
                        Toast.makeText(this, "Action failed!! Please try again...", Toast.LENGTH_SHORT).show()
                    }

                }
            }else if(requestCode == CAMERA){

                try{
                    val thumbnail : Bitmap = data!!.extras!!.get("data") as Bitmap
                    saveImageToInternalStorage(thumbnail)
                    binding?.ivPlaceImage!!.setImageBitmap(thumbnail)
                    Toast.makeText(this, "actioncompleted", Toast.LENGTH_SHORT).show()
                }catch(e:Exception){
                    e.printStackTrace()
                    Toast.makeText(this, "Action Failed!!", Toast.LENGTH_SHORT).show()
                }

            }
            else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {

                val place: Place = Autocomplete.getPlaceFromIntent(data!!)

                binding?.etLocation?.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                  Log.e("Cancelled", "Cancelled")
            }
        }
    }

    private fun takePhotoFromCamera(){
        Dexter.withContext(this).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                if(report!!.areAllPermissionsGranted()){



                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                p1: PermissionToken?
            ) {
                showRationaleDialogForPermissions()
            }

        }).onSameThread().check()

    }
    private fun choosePhotoFromGallery(){
        Dexter.withContext(this).withPermissions(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(report: MultiplePermissionsReport?) {

                if(report!!.areAllPermissionsGranted()){

                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                p1: PermissionToken?
            ) {
                showRationaleDialogForPermissions()
            }

        }).onSameThread().check()

    }

    private fun saveImageToInternalStorage(bitmap:Bitmap):Uri{

        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY,Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try{

            val stream : OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()


        }catch(e:IOException){
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath)

    }

    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat,Locale.getDefault())
        binding?.etDate?.setText(sdf.format(cal.time).toString())
    }
    private fun showRationaleDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("Permissions Denied").setPositiveButton("Go to Settings") { _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel") { dialog,
                                           _ ->
                dialog.dismiss()
            }.show()
    }

    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "FavouritePlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}