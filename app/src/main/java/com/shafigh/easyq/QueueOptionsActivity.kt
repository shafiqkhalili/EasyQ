package com.shafigh.easyq

import android.location.Address
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
import kotlinx.android.synthetic.main.activity_queue_options.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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

        // Define a Place ID.
        val placeId = "ChIJAysjm0xwX0YROPjAzVbwv6M"

        // Initialize Places.
        Places.initialize(applicationContext, "AIzaSyBhE-W27N5l__Pz4Ny1DrkXdC0irG5R37c")
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
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

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

        val options = listOf(QueueTypes("Beard",1,0),
            QueueTypes("Hair",3,1),
        QueueTypes("Wax",2,0))
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewQueueOptions)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = QueueOptionsAdapter(context = this, options = options)

        recyclerView.adapter = adapter
    }
}
