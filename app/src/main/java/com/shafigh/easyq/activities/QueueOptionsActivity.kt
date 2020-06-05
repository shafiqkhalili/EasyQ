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
import com.google.firebase.firestore.FirebaseFirestore
import com.shafigh.easyq.R
import com.shafigh.easyq.adapters.QueueOptionsAdapter
import com.shafigh.easyq.modules.Constants
import com.shafigh.easyq.modules.Queue
import com.shafigh.easyq.modules.QueueOptions
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


class QueueOptionsActivity : AppCompatActivity() {
    private lateinit var textViewHeader: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewDate: TextView
    private var placeId: String? = null
    private var queueOptions = mutableListOf<QueueOptions>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue_options)

        var db = FirebaseFirestore.getInstance()

        textViewHeader = findViewById(R.id.textViewBusiness)
        textViewAddress = findViewById(R.id.textViewAddress)
        textViewDate = findViewById(R.id.textViewDate)
        placeId = intent.getStringExtra(R.string.place_id.toString())

        //Firebase variables
        var queues = mutableListOf<Queue>()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewQueueOptions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        var todayDate = Calendar.getInstance()

        todayDate.set(Calendar.HOUR_OF_DAY, 0)
        todayDate.set(Calendar.MINUTE, 0)
        todayDate.set(Calendar.SECOND, 0)
        todayDate.set(Calendar.MILLISECOND, 0)
        val todayMillSecs = todayDate.time

        // Initialize Places.
        placeId?.let { placeId ->
            //Get info about POI from Google API
            poiInfo(placeId)
            //Check if POI exists
            val queueOptCollectionRef =
                db.collection(Constants.POI_COLLECTION).document(placeId)
                    .collection(Constants.QUEUE_OPTION_COLLECTION)

            queueOptCollectionRef.addSnapshotListener { snap, e ->
                if (snap == null || snap.size() == 0) {
                    val queueOpt = QueueOptions()
                    //Add POI to Firebase
                    queueOptCollectionRef.add(queueOpt)
                        .addOnSuccessListener {
                            println("Added QueueOpt")
                        }
                        .addOnFailureListener { error ->
                            println("Error on adding QueueOpt ${error.localizedMessage}")
                        }
                } else {
                    for (document in snap.documents) {
                        val queueOpt = document.toObject(QueueOptions::class.java)
                        if (queueOpt != null) {
                            queueOptCollectionRef.document(document.id)
                                .collection(Constants.QUEUE_COLLECTION)
                                .whereGreaterThanOrEqualTo("issuedAt", todayMillSecs).get()
                                .addOnSuccessListener { qs ->
                                    for (doc in qs) {
                                        try {
                                            val q = doc.toObject(Queue::class.java)
                                            q.uid = doc.id
                                            //if user has active queue place
                                            queues.add(q)
                                        } catch (e: Exception) {
                                            println("Error on casting snapshot to Queue object : ${e.localizedMessage}")
                                        }
                                    }
                                    println("qSize  ${queues.size}")
                                    var latestDone = queues.indexOfLast { q -> q.done }
                                    println("latestDone: $latestDone")
                                    if (latestDone < 0){
                                        latestDone = 0
                                    }
                                    queueOpt.servingNow = latestDone + 1
                                    queueOpt.availableNr = queues.size + 1
                                    queueOpt.averageTime = queueOpt.averageTime

                                    queueOpt.queueOptDocId = document.id
                                    queueOpt.poiDocId = placeId as String
                                    queueOptions.add(queueOpt)
                                    recyclerView.adapter?.notifyDataSetChanged()
                                }
                        }
                    }
                }
                if (e != null) {
                    println("Error: ${e.localizedMessage}")
                }
                val adapter = QueueOptionsAdapter(
                    context = applicationContext,
                    queueOptions = queueOptions
                )
                recyclerView.adapter = adapter
            }
        }

    }

    fun poiInfo(placeId: String): Unit {
        Places.initialize(this, Constants.MAP_API)

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
        try {
            val request = FetchPlaceRequest.newInstance(placeId, placeFields)
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
