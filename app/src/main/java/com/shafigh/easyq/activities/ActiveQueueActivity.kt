package com.shafigh.easyq.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.*
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
    private var queueOption: QueueOptions? = null
    private var queues = mutableListOf<Queue>()

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private var token: String = "null"
    private var newQueue: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_queue)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!

        token = Helpers.getUidFromSharedPref(applicationContext)

        println("oldToken ID $token")

        //Places info
        textViewHeader = findViewById(R.id.textViewBusiness)
        textViewAddress = findViewById(R.id.textViewAddress)
        textViewDate = findViewById(R.id.textViewDate)
        //queue info
        textViewOptionName = findViewById(R.id.textViewOptionName)
        textViewYourNr = findViewById(R.id.textViewYourNr)
        buttonCancel = findViewById(R.id.buttonCancel)
        textViewEstimate = findViewById(R.id.textViewEstimatedTime)
        textViewServingNow = findViewById(R.id.textViewServingNow)

        try {
            queueOption =
                intent.getSerializableExtra(R.string.QUEUE_OPTIONS_OBJ.toString()) as QueueOptions
            queueOption?.let { queueOption ->
                try {
                    println("opts: $queueOption")
                    textViewOptionName.text = queueOption.name
                    //Get queue from Firebase
                    token.let {
                        try {
                            Firestore.db.collection(Constants.POI_COLLECTION)
                                .document(queueOption.poiDocId)
                                .collection(Constants.QUEUE_OPTION_COLLECTION)
                                .document(queueOption.queueOptDocId)
                                .collection(Constants.QUEUE_COLLECTION)
                                .addSnapshotListener { docRef, e ->
                                    if (e != null) {
                                        return@addSnapshotListener
                                    }
                                    for (doc in docRef!!) {
                                        val q = doc.toObject(Queue::class.java)
                                        if (doc.id == token) {
                                            println("newQ: ${doc.id}")
                                            newQueue = false
                                            queue = q
                                        }
                                        queues.add(q)
                                    }
                                }
                        } catch (e: Exception) {
                            println("Error on ActiveQueueuAcitivity: ${e.localizedMessage}")
                        }
                    }
                } catch (e: Exception) {
                    println("Line 107: " + e.localizedMessage)
                }
            }
        } catch (e: Exception) {
            println("Line 111: " + e.localizedMessage)
        }
        queueOption?.poiDocId.let { placeId ->
            println("Map api: ${Constants.MAP_API}")
            //Get info about POI from Google API
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
            //user.delete()
            //Remove user form Queue
            queueOption?.let { queueOption ->
                Firestore.db.collection(Constants.POI_COLLECTION)
                    .document(queueOption.poiDocId)
                    .collection(Constants.QUEUE_OPTION_COLLECTION)
                    .document(queueOption.queueOptDocId)
                    .collection(Constants.QUEUE_COLLECTION).document(token)
                    .delete()
                    .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully deleted!") }
                    .addOnFailureListener { e -> Log.w("TAG", "Error deleting document", e) }
            }
            val intent = Intent(this, MapsActivity::class.java)
            this.startActivity(intent)
        }
    }

    fun addQueue(docRef: DocumentReference): Unit {

        queue = Queue()
        println("DocumentSnapshot data empty")
        queueOption?.poiDocId?.let {
            Firestore.db.collection(Constants.POI_COLLECTION)
                .document(it)
                .collection(Constants.QUEUE_OPTION_COLLECTION)
                .document(queueOption!!.queueOptDocId)
                .collection(Constants.QUEUE_COLLECTION).document(token)
                .set(queue)
                .addOnSuccessListener { documentReference ->
                    println("DocumentSnapshot written : $documentReference")
                }.addOnFailureListener { e ->
                    println("Error line 151" + e.localizedMessage)
                }
        }

    }

    fun poiInfo(placeId: String, context: Context): Unit {
        //Get info about POI from Google API
        Places.initialize(context, Constants.MAP_API)

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formattedDate = current.format(formatter)
        // Create a new Places client instance.
        val placesClient = Places.createClient(context)
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

    fun queuePosition(uidRef: Any, placeId: String): Int {
        return 0
    }
}

