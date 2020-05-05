package com.shafigh.easyq.activities

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.shafigh.easyq.CustomInfoWindowAdapter
import com.shafigh.easyq.R
import java.io.IOException
import java.util.ArrayList

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowClickListener {
    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
    }

    private lateinit var map: GoogleMap

    //most recent location currently available.
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private val zoomLevel = 15f
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    //Widget
    private lateinit var mSearchText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        println("oncreate called")
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)

                lastLocation = p0.lastLocation
                placeMarkerOnMap(LatLng(lastLocation.latitude, lastLocation.longitude))
            }
        }
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()

        //For tracking location
        //createLocationRequest()

        mSearchText = findViewById(R.id.input_search)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        setMapClick(map)

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        //Zoom to current location
        setUpMap()
        init()
    }

    //On LongClick add Pin
    private fun setMapClick(map: GoogleMap) {
        map.setOnMapClickListener { latLng ->
            map.clear()
            placeMarkerOnMap(latLng)
        }
    }

    private fun setUpMap() {
        // checks if the app has been granted the ACCESS_FINE_LOCATION permission.
        checkPermission()
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.isMyLocationEnabled = true
        //Current location
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, zoomLevel))
            }
        }
        setPadding()
    }

    //Marker
    private fun placeMarkerOnMap(location: LatLng) {
        map.clear()
        map.setInfoWindowAdapter(CustomInfoWindowAdapter())

        val markerOptions = MarkerOptions().position(location)

        val titleStr = getAddress(location)  // add these two lines
        markerOptions.title(titleStr)
        map.addMarker(markerOptions)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel))
    }

    //takes the coordinates of a location and returns a readable address
    private fun getAddress(latLng: LatLng): String {
        // 1
        val geocoder = Geocoder(this)
        val addresses: List<Address>?
        val address: Address?
        var addressText = ""

        try {
            // 2
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            // 3
            if (null != addresses && addresses.isNotEmpty()) {
                address = addresses[0]
                for (i in 0 until address.maxAddressLineIndex) {
                    addressText += if (i == 0) address.getAddressLine(i) else "\n" + address.getAddressLine(
                        i
                    )
                }
            }
        } catch (e: IOException) {
            Log.e("MapsActivity", e.localizedMessage)
        }

        return addressText
    }

    // checks if the app has been granted the ACCESS_FINE_LOCATION permission.
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //if not granted, ask for permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
    }

    override fun onInfoWindowClick(p0: Marker?) {

    }

    override fun onMarkerClick(p0: Marker?): Boolean {
        return false
    }

    private fun setPadding() {
        map.setPadding(0, 200, 0, 0)
    }

    /*Search setup*/
    private fun init() {
        println("Init called")
        hideKeyboard()
        mSearchText.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                keyEvent.action == KeyEvent.ACTION_DOWN || keyEvent.action == KeyEvent.KEYCODE_ENTER
            ) {
                println("keyEvent.action: $keyEvent.action")
                //execute our method for searching
                geoLocate()
            }
            return@setOnEditorActionListener false
        }
    }
    //Search place by string
    private fun geoLocate() {
        println("geoLocate called")
        val searchString = mSearchText.text.toString()
        val geocoder = Geocoder(this@MapsActivity)
        var list: List<Address> =
            ArrayList()
        try {
            list = geocoder.getFromLocationName(searchString, 1)
        } catch (e: IOException) {
            Log.e("Address",e.localizedMessage)
        }
        if (list.isNotEmpty()) {
            val address = list[0]
            Log.e("LOCATION",address.toString())
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            placeMarkerOnMap(LatLng(address.latitude,address.longitude))
            hideKeyboard()
        }
    }

    private fun AppCompatActivity.hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        // else {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        // }
    }

    /*Location tracking setup*/
    /*
    //if the ACCESS_FINE_LOCATION permission has not been granted, request it.
    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }
    //retrieve and handle any changes to be made based on the current state of the user’s location
    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 10000
        // 3
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            // 6
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    // 1
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }
    */
}
