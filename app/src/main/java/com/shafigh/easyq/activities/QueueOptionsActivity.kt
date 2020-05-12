package com.shafigh.easyq.activities

import android.content.Context
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
import com.shafigh.easyq.BuildConfig
import com.shafigh.easyq.QueueOptionsAdapter
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.Constants
import com.shafigh.easyq.modules.Constants.Companion.MAP_API
import com.shafigh.easyq.modules.DataManager
import com.shafigh.easyq.modules.Firestore
import com.shafigh.easyq.modules.Firestore.readAllQueueOptions
import com.shafigh.easyq.modules.QueueOptions
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class QueueOptionsActivity : AppCompatActivity() {

    companion object {
        var queueOptId: String? = null
    }

    private lateinit var textViewHeader: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewDate: TextView
    private var placeId: String? = null
    private var queueOptions = mutableListOf<QueueOptions>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue_options)

        textViewHeader = findViewById(R.id.textViewBusiness)
        textViewAddress = findViewById(R.id.textViewAddress)
        textViewDate = findViewById(R.id.textViewDate)
        placeId = intent.getStringExtra(R.string.place_id.toString())

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewQueueOptions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize Places.
        placeId?.let {
            //Get info about POI from Google API
            Places.initialize(this, MAP_API)

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
                val request = placeId.let { FetchPlaceRequest.newInstance(placeId!!, placeFields) }
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
            //Retrieve data from Firestore and keep in Datamanager
            Firestore.db.collection(Firestore.poiCollection).document(placeId!!)
                .collection(Firestore.queueOptCollection).addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("TAG", "Listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        queueOptions.clear()
                        for (document in snapshot.documents) {
                            val queueOpt = document.toObject(QueueOptions::class.java)
                            if (queueOpt != null) {
                                Log.d("isSuccess ", document.id)
                                queueOpt.queueDocId = document.id
                                queueOpt.placeDocId = placeId as String
                                queueOptions.add(queueOpt)
                                recyclerView.adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
        }

        val adapter = QueueOptionsAdapter(
            context = applicationContext,
            queueOptions = queueOptions
        )
        recyclerView.adapter = adapter
    }
}