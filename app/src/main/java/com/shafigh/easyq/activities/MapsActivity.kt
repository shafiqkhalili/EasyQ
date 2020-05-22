package com.shafigh.easyq.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.telephony.TelephonyManager
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.shafigh.easyq.CustomInfoWindowAdapter
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.Constants
import com.shafigh.easyq.modules.Helpers
import java.io.IOException
import java.time.LocalDate
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnPoiClickListener, GoogleMap.OnMapClickListener {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
        private const val PLACE_PICKER_REQUEST = 3
    }

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null

    var placeId: String? = null
    private lateinit var map: GoogleMap

    //most recent location currently available.
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private val zoomLevel = 15f
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false
    private lateinit var selectedMarker: Marker
    private lateinit var buttonSeeQueues: Button
    private lateinit var textSelectPoi: TextView
    private lateinit var textOpenHours: TextView

    private var userUUID: String? = null

    //Widget
    private lateinit var mSearchText: AutoCompleteTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        mSearchText = findViewById(R.id.input_search)
        buttonSeeQueues = findViewById(R.id.buttonSeeQueues)
        textSelectPoi = findViewById(R.id.textViewPoiName)
        textOpenHours = findViewById(R.id.textViewOpenHour)

        // Initialize Firebase Auth
        try {
            if (user == null) {
                try {
                    auth.signInAnonymously()
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                user = auth.currentUser
                                userUUID = user?.uid
                                userUUID?.let { Helpers.setUidInSharedPref(it, applicationContext) }
                                println("uid: $userUUID")
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "signInAnonymously:failure", task.exception)
                                Toast.makeText(
                                    baseContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } catch (e: Exception) {
                    println("Sign in error: ${e.localizedMessage}")
                    return
                }
            } else {
                println("currentUserID: ${user!!.uid}")
                user = auth.currentUser
                userUUID = user?.uid
                userUUID?.let { Helpers.setUidInSharedPref(it, applicationContext) }
                println("uid: $userUUID")
            }
        } catch (e: Exception) {
            println("Init auth error: ${e.localizedMessage}")
            return
        }
        //signIn()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Location track
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {

                lastLocation = locationResult.lastLocation
            }
        }
        //For tracking location
        createLocationRequest()
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

        setUpMap()
        initSearch()

        val customInfoWindow = CustomInfoWindowAdapter(this)
        map.setInfoWindowAdapter(customInfoWindow)
        //On clicking Queue Button
        buttonSeeQueues.setOnClickListener {
            val intent = Intent(this, QueueOptionsActivity::class.java)
            intent.putExtra(R.string.place_id.toString(), placeId)
            this.startActivity(intent)
        }
        //On clicking on a Google Place
        map.setOnPoiClickListener(this)
        //custom marker info window
        val adapter = CustomInfoWindowAdapter(this)
        map.setInfoWindowAdapter(adapter)
    }

    private fun setUpMap() {
        // checks if the app has been granted the ACCESS_FINE_LOCATION permission.
        map.mapType = GoogleMap.MAP_TYPE_NORMAL
        map.isMyLocationEnabled = true
        //If permission is allowed, get last location
        if (checkPermission()) {
            fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    lastLocation = location
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, zoomLevel))
                }
            }
        }
        map.uiSettings.isZoomControlsEnabled = true
        //map.setOnMarkerClickListener(this)
        setPadding()
    }

    //Marker
    private fun placeLocationMarkerOnMap(location: LatLng) {
        map.clear()
        selectedMarker = map.addMarker(
            MarkerOptions()
                .position(location)
        )
    }

    private fun placePoiMarkerOnMap(poi: PointOfInterest?) {
        map.clear()
        //val icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_location_marker_primary)
        if (poi != null) {
            placeId = poi.placeId
            selectedMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .snippet(poi.placeId)
            )
            selectedMarker.showInfoWindow()
            poiInfo(poi.placeId)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(poi.latLng, zoomLevel))
        }
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
    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            //if not granted, ask for permission
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    private fun setPadding() {
        map.setPadding(0, 200, 0, 350)
    }

    /*On enter button , search map*/
    private fun initSearch() {
        hideKeyboard()
        mSearchText.setOnEditorActionListener { _, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
                keyEvent.action == KeyEvent.ACTION_DOWN || keyEvent.action == KeyEvent.KEYCODE_ENTER
            ) {
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
            Log.e("Address", e.localizedMessage)
        }
        if (list.isNotEmpty()) {
            val address = list[0]
            Log.e("LOCATION", address.toString())
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        address.latitude,
                        address.longitude
                    ), zoomLevel
                )
            )
            placeLocationMarkerOnMap(LatLng(address.latitude, address.longitude))
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

    override fun onPoiClick(poi: PointOfInterest?) {
        placePoiMarkerOnMap(poi)
    }


    fun getUsersIMEI(): String {
        /*
        * getDeviceId() returns the unique device ID.
        * For example,the IMEI for GSM and the MEID or ESN for CDMA phones.
        */
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_PHONE_STATE),
                1
            )
        }
        val telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        return telephonyManager.imei

    }

    /*Location tracking setup*/
    private fun startLocationUpdates() {
        //1  if the ACCESS_FINE_LOCATION permission has not been granted, request it.
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper() /* Looper */
        )
    }

    //retrieve and handle any changes to be made based on the current state of the user’s location
    private fun createLocationRequest() {

        locationRequest = LocationRequest()

        locationRequest.interval = 10000

        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

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
                    e.startResolutionForResult(
                        this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            println("permission granted")
            startLocationUpdates()
        } else {
            println("Permission denied")
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }

    override fun onMapClick(p0: LatLng?) {
        placeId = null
        map.clear()
        selectedMarker.remove()
        hideKeyboard()
    }

    private fun signIn(): Unit {
        auth.createUserWithEmailAndPassword("test@easyq.se", "password")
            .addOnCompleteListener(
                this
            ) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    println("CreateUserWithEmail:success")
                    //user = auth.currentUser!!
                } else {
                    // If sign in fails, display a message to the user.
                    println(
                        "createUserWithEmail:failure ${task.exception}"
                    )
                }
            }
    }

    fun poiInfo(placeId: String) {
        var placeInfo: Place? = null
        var openHour: String = "00"
        var openMinutes: String = "00"
        var closeHour: String = "00"
        var closeMinutes: String = "00"

        Places.initialize(this, Constants.MAP_API)
        val dayInt = LocalDate.now().dayOfWeek.value

        // Create a new Places client instance.
        val placesClient = Places.createClient(this)
        // Specify the fields to return.
        val placeFields: List<Place.Field> =
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.OPENING_HOURS,
                Place.Field.PHONE_NUMBER,
                Place.Field.PRICE_LEVEL,
                Place.Field.UTC_OFFSET
            )
        try {
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)
            try {
                placesClient.fetchPlace(request).addOnSuccessListener { response ->
                    placeInfo = response.place
                    placeInfo?.let { place ->
                        val openHours = place.openingHours?.periods?.get(dayInt)?.open?.time
                        openHours?.let {
                            openHour = openHours.hours.toString().padStart(2, '0')
                            openMinutes = openHours.minutes.toString().padStart(2, '0')
                        }
                        val closeHours = place.openingHours?.periods?.get(dayInt)?.close?.time
                        closeHours?.let {
                            closeHour = closeHours.hours.toString().padStart(2, '0')
                            closeMinutes = closeHours.minutes.toString().padStart(2, '0')
                        }

                        val stringB = StringBuilder()
                        stringB.append(openHour)
                        stringB.append(":")
                        stringB.append(openMinutes)
                        stringB.append(" to ")
                        stringB.append(closeHour)
                        stringB.append(":")
                        stringB.append(closeMinutes)

                        textSelectPoi.text = place.name
                        textOpenHours.text = stringB
                        place.isOpen?.let {
                            println("isOpen: $it")
                            if (it) {
                                buttonSeeQueues.isEnabled = true
                            }
                        }
                        println("Place: $place ")
                    }
                }.addOnFailureListener { exception ->
                    if (exception is ApiException) {
                        val statusCode = exception.statusCode
                        // Handle error with given status code.
                        Log.e(
                            "DEMO",
                            "API: $placeId, Place not found: " + exception.localizedMessage
                        )
                    }
                }
            } catch (e: Exception) {
                println(e.localizedMessage)
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        }
    }
    /*Autocomplete setup*/
    /*
    fun autoCompleteIntent(): Unit {
        val AUTOCOMPLETE_REQUEST_CODE = 1

        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields = listOf(
            Place.Field.ID,
            Place.Field.NAME
        )

        // Start the autocomplete intent.
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.FULLSCREEN, fields
        )
            .build(this)
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
    }
    private fun initAutoCompleteFragment() {

        // Initialize the SDK
        Places.initialize(applicationContext, PLACE_API)

        // Create a new Places client instance
        val placesClient: PlacesClient = Places.createClient(this)

        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?

        // Specify the types of place data to return.
        autocompleteFragment?.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME))
        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(
                    "AC",
                    "Place: " + place.name.toString() + ", " + place.getId()
                )
            }

            override fun onError(p0: Status) {
                TODO("Not yet implemented")
            }

        })
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val place: Place? = data?.let { Autocomplete.getPlaceFromIntent(it) }
                if (place != null) {
                    Log.i("AutoComp",
                        "Place: " + place.name + ", " + place.id
                    )
                }
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                val status: Status? = data?.let { Autocomplete.getStatusFromIntent(it) }
                if (status != null) {
                    Log.i("AutoComp", status.statusMessage)
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the operation.
                println("AutoComp resultCode error ")
            }
        }
    }
   */
}
