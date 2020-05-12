package com.shafigh.easyq.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.Firestore
import com.shafigh.easyq.modules.Queue
import com.shafigh.easyq.modules.QueueOptions
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ActiveQueueActivity : AppCompatActivity() {

    private lateinit var textViewHeader: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewDate: TextView

    private lateinit var textViewOptionName: TextView
    private lateinit var textViewYourNr: TextView
    private lateinit var buttonCancel: Button
    private lateinit var textViewEstimate: TextView
    private lateinit var textViewServingNow: TextView
    private lateinit var queue: Queue
    private var queueOptions: QueueOptions? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_queue)

        //Places info
        try {
            textViewHeader = findViewById(R.id.textViewBusiness)
            textViewAddress = findViewById(R.id.textViewAddress)
            textViewDate = findViewById(R.id.textViewDate)
        } catch (e: Exception) {
            println("Line 49 ${e.localizedMessage}")
        }
        //queue info
        textViewOptionName = findViewById(R.id.textViewOptionName)
        textViewYourNr = findViewById(R.id.textViewYourNr)
        buttonCancel = findViewById(R.id.buttonCancel)
        textViewEstimate = findViewById(R.id.textViewEstimatedTime)
        textViewServingNow = findViewById(R.id.textViewServingNow)
        try {
            queueOptions = (intent.getSerializableExtra("QUEUE_OPTION") as? QueueOptions)
            if (queueOptions != null) {
                fetchPoiInfo(placeId = queueOptions!!.placeDocId)
                try {
                    textViewOptionName.text = queueOptions!!.name.toString()
                    val queue = Queue()
                    //Get queue from Firebase
                    Firestore.db.collection(Firestore.poiCollection)
                        .document(queueOptions!!.placeDocId)
                        .collection(Firestore.queueOptCollection).document(queueOptions!!.queueDocId)
                        .set(queue)
                        .addOnSuccessListener {
                            Log.d(
                                "Data",
                                "DocumentSnapshot added"
                            )
                            return@addOnSuccessListener
                        }
                        .addOnFailureListener { e ->
                            Log.w("Data", "Error adding document", e)
                            return@addOnFailureListener
                        }
                } catch (e: Exception) {
                    println("Error on ActiveQueueuAcitivity: ${e.localizedMessage}")
                }
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        }

        try {
            textViewYourNr.text = "5"
        } catch (e: Exception) {
            println("Line 88 ${e.localizedMessage}")
        }
        /* if (queueOptions != null) {
             textViewEstimate.text = queueOptions.time.toString()
         }*/
        /*if (queueOptions != null) {
            textViewServingNow.text = queueOptions.servingNow.toString()
        }*/
        buttonCancel.setOnClickListener {
            val intent = Intent(this, QueueOptionsActivity::class.java)

            this.startActivity(intent)
        }
    }

    private fun placeInfoold() {
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
        try {
            // Construct a request object, passing the place ID and fields array.
            val request =
                MapsActivity.placeId?.let { FetchPlaceRequest.newInstance(it, placeFields) }
            println("PlaceID ActiveQ: ${MapsActivity.placeId}")

            if (request != null) {
                try {
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
                            Log.e("DEMO", "Place not found: " + exception.localizedMessage)
                        }
                    }
                } catch (e: Exception) {
                    println(e.localizedMessage)
                }
            }
        } catch (e: Exception) {
            println(e.localizedMessage)
        }
    }

    private fun fetchPoiInfo(placeId: String): Unit {
        Places.initialize(applicationContext, placeId)

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formattedDate = current.format(formatter)
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
        try {
            val request = placeId.let { FetchPlaceRequest.newInstance(it, placeFields) }
            try {
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
}
