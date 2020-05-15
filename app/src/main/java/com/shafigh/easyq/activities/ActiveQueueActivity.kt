package com.shafigh.easyq.activities

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
import com.google.firebase.firestore.CollectionReference
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
    private var queue: Queue? = null
    private var queueOption: QueueOptions? = null
    private var queues = mutableListOf<Queue>()

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser
    private var uID: String = "null"
    private var newQueue: Boolean = true
    private var queueCollectionRef: CollectionReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_queue)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!

        uID = Helpers.getUidFromSharedPref(applicationContext)
        queueOption =
            intent.getSerializableExtra(R.string.QUEUE_OPTIONS_OBJ.toString()) as QueueOptions
        println("oldToken ID $uID")

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

        textViewOptionName.text = queueOption?.name

        queueOption?.poiDocId?.let { placeId ->
            println("Map api: ${Constants.MAP_API}")
            //Get info about POI from Google API
            poiInfo(placeId)
        }
        queueOption?.let { queueOption ->
            queueCollectionRef = Firestore.db.collection(Constants.POI_COLLECTION)
                .document(queueOption.poiDocId)
                .collection(Constants.QUEUE_OPTION_COLLECTION)
                .document(queueOption.queueOptDocId)
                .collection(Constants.QUEUE_COLLECTION)

            getAllQueues(queueOption)
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
            queueCollectionRef?.let {
                it.document(uID).delete()
                    .addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully deleted!") }
                    .addOnFailureListener { e -> Log.w("TAG", "Error deleting document", e) }
            }
            val intent = Intent(this, MapsActivity::class.java)
            this.startActivity(intent)
        }
    }

    private fun getAllQueues(queueOption: QueueOptions): Unit {

        try {
            println("opts: $queueOption")

            //Get queue from Firebase
            queueCollectionRef?.let { collectionRef ->
                uID.let { uID ->
                    //Get all Queues
                    try {
                        collectionRef.addSnapshotListener { docRef, e ->
                            if (e != null) {
                                return@addSnapshotListener
                            }
                            for (doc in docRef!!) {
                                val q = doc.toObject(Queue::class.java)
                                if (doc.id == uID) {
                                    println("newQ: ${doc.id}")
                                    newQueue = false
                                    queue = q
                                }
                                queues.add(q)
                            }
                            if (newQueue) {
                                addQueueToFirestore()
                            }
                        }
                    } catch (e: Exception) {
                        println("Error on ActiveQueueuAcitivity: ${e.localizedMessage}")
                    }
                }
            }
        } catch (e: Exception) {
            println("Line 107: " + e.localizedMessage)
        }
    }

    private fun addQueueToFirestore() {
        //New queue instance
        val q = Queue()
        println("Adding Q to Firestore : $q")

        try {
            queueCollectionRef?.let {
                it.document(uID).set(q).addOnSuccessListener {
                    println("DocumentSnapshot written ")
                }.addOnFailureListener { e ->
                    println("Error line 151" + e.localizedMessage)
                }
            }
        } catch (e: java.lang.Exception) {
            print("Error on adding new Q: ${e.localizedMessage}")
        }
    }

    private fun poiInfo(placeId: String): Unit {
        //Get info about POI from Google API
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
                    println("Place found: " + place.address)
                    textViewHeader.text = place.name
                    textViewAddress.text = place.address
                    textViewDate.text = formattedDate.toString()

                }.addOnFailureListener { exception ->
                    if (exception is ApiException) {
                        val statusCode = exception.statusCode
                        // Handle error with given status code.
                        println(
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

