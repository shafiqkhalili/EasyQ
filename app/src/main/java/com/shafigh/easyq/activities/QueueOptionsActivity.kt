package com.shafigh.easyq.activities

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.shafigh.easyq.QueueOptionsAdapter
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.DataManager
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

const val PLACE_API = "AIzaSyCdgwD6mOCOF6hnR0QUSCOmd_VDPflbnU4"

class QueueOptionsActivity : AppCompatActivity() {

    private lateinit var textViewHeader : TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewDate : TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue_options)

        val uuid: UUID = UUID.randomUUID()
        val variant: Int = uuid.variant()
        val version: Int = uuid.version()
        println("Variant: $variant")
        println("Versin: $version")

        textViewHeader = findViewById(R.id.textViewBusiness)
        textViewAddress = findViewById(R.id.textViewAddress)
        textViewDate = findViewById(R.id.textViewDate)

        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formattedDate = current.format(formatter)

        // Initialize Places.
        MapsActivity.placeId?.let { Places.initialize(applicationContext, it) }
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
                Place.Field.PRICE_LEVEL
            )
        // Construct a request object, passing the place ID and fields array.
        val request = MapsActivity.placeId?.let { FetchPlaceRequest.newInstance(it, placeFields) }
        println("PlaceID Qoption: ${MapsActivity.placeId}")

        if (request != null) {
            placesClient.fetchPlace(request).addOnSuccessListener { response ->
                val place: Place = response.place
                Log.i("DEMO", "Place found: " + place.address)
                textViewHeader.text = place.name
                textViewAddress.text = place.address
                textViewDate.text = formattedDate.toString()

            }.addOnFailureListener { exception ->
                if (exception is ApiException) {
                    val statusCode = exception.statusCode
                    // Handle error with given status code.
                    Log.e("DEMO","Place not found: " + exception.localizedMessage)
                }
            }
        }

        val options = DataManager.getQueueTypes()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewQueueOptions)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = QueueOptionsAdapter(
            context = this,
            queueTypes = options
        )

        recyclerView.adapter = adapter
    }
}
