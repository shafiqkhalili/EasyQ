package com.shafigh.easyq

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.shafigh.easyq.modules.DataManager
import com.shafigh.easyq.modules.QueueTypes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

const val PLACE_ID = "ChIJAysjm0xwX0YROPjAzVbwv6M"
const val PLACE_API = "AIzaSyCdgwD6mOCOF6hnR0QUSCOmd_VDPflbnU4"

class QueueOptionsActivity : AppCompatActivity() {

    lateinit var textViewHeader : TextView
    lateinit var textViewAddress: TextView
    lateinit var textViewDate : TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue_options)

        textViewHeader = findViewById(R.id.textViewBusiness)
        textViewAddress = findViewById(R.id.textViewAddress)
        textViewDate = findViewById(R.id.textViewDate)

        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formattedDate = current.format(formatter)

        // Initialize Places.
        Places.initialize(applicationContext, PLACE_API)
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
        val request = FetchPlaceRequest.newInstance(PLACE_ID, placeFields)

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

        val options = DataManager.getQueueTypes()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewQueueOptions)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = QueueOptionsAdapter(context = this, options = options)

        recyclerView.adapter = adapter
    }
}
