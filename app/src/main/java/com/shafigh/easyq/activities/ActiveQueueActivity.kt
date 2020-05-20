package com.shafigh.easyq.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.*
import com.shafigh.easyq.modules.Queue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


class ActiveQueueActivity : AppCompatActivity() {

    //textViews
    private lateinit var textViewHeader: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewDate: TextView
    private lateinit var textViewOptionName: TextView
    private lateinit var textViewYourNr: TextView
    private lateinit var buttonCancel: Button
    private lateinit var textViewEstimate: TextView
    private lateinit var textViewServingNow: TextView
    private lateinit var textViewAhead: TextView

    //Variables
    private var queue: Queue? = null
    private var queueOption: QueueOptions? = null
    private var queues = mutableListOf<Queue>()
    private var servingNow: Int = 0
    private var averageTime: Int = 0
    private var userPosition: Int = 0
    private var aheadOfUser: Int = 0

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
        textViewAhead = findViewById(R.id.textViewAhead)
        textViewOptionName.text = queueOption?.name

        queueOption?.let { queueOption ->
            poiInfo(queueOption.poiDocId)
            queueCollectionRef = Firestore.db.collection(Constants.POI_COLLECTION)
                .document(queueOption.poiDocId)
                .collection(Constants.QUEUE_OPTION_COLLECTION)
                .document(queueOption.queueOptDocId)
                .collection(Constants.QUEUE_COLLECTION)

            getAllQueues(queueOption)
        }

        buttonCancel.setOnClickListener {
            //Change queue status to Done
            queue?.done = true
            queue?.uid?.let { uid ->
                queueCollectionRef?.let {
                    it.document(uid).set(queue!!, SetOptions.merge())
                        .addOnSuccessListener { println("DocumentSnapshot successfully deleted!") }
                        .addOnFailureListener { e -> println("Error deleting document: " + e.localizedMessage) }
                }
            }
            val intent = Intent(this, MapsActivity::class.java)
            this.startActivity(intent)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getAllQueues(queueOption: QueueOptions): Unit {
        try {
            var todayDate = Calendar.getInstance()

            todayDate.set(Calendar.HOUR_OF_DAY, 0)
            todayDate.set(Calendar.MINUTE,0)
            todayDate.set(Calendar.SECOND,0)
            todayDate.set(Calendar.MILLISECOND,0)
            val todayMillSecs = todayDate.time
            val dayInMillis = 24 * 60 * 60 * 1000
            println("Today: $todayMillSecs")
            //Get queues from Firebase
            queueCollectionRef?.let { collectionRef ->
                uID.let { uID ->
                    //Get all Queues
                    try {
                        collectionRef.orderBy("issuedAt", Query.Direction.ASCENDING)
                            .whereGreaterThanOrEqualTo("issuedAt", todayMillSecs)
                            .addSnapshotListener { docRef, e ->
                                if (e != null) {
                                    println("SnapshotListener: ${e.localizedMessage}")
                                    return@addSnapshotListener
                                }
                                //Reset
                                resetProperties()
                                for (doc in docRef!!) {
                                    try {
                                        val q = doc.toObject(Queue::class.java)
                                        q.uid = doc.id
                                        println("Queue: $q")
                                        //if user has active queue place
                                        if (doc.id == uID && !q.done) {
                                            newQueue = false
                                            queue = q
                                        }
                                        queues.add(q)
                                    } catch (e: Exception) {
                                        println("Error on casting snapshot to Queue object : ${e.localizedMessage}")
                                    }
                                }
                                if (newQueue) {
                                    addQueueToFirestore()
                                }
                                try {
                                    userPosition = queues.indexOf(queue)
                                    servingNow = queues.indexOfFirst { q -> !q.done }
                                    aheadOfUser =
                                        (servingNow until userPosition).filter { q -> !queues[q].done }.size
                                } catch (e: java.lang.Exception) {
                                    println(e.localizedMessage)
                                }
                                userPosition++
                                textViewYourNr.text = userPosition.toString().padStart(3, '0')
                                servingNow++
                                textViewServingNow.text = servingNow.toString().padStart(3, '0')
                                textViewAhead.text = aheadOfUser.toString().padStart(3, '0')
                                textViewEstimate.text =
                                    (queueOption.averageTime * aheadOfUser).toString() + " minutes"
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
        try {
            q.issuedAt = FieldValue.serverTimestamp()
        } catch (e: java.lang.Exception) {
            println("Ex: ${e.localizedMessage}")
        }

        try {
            queueCollectionRef?.let {
                it.document(uID).set(q).addOnSuccessListener {
                    //val ts = FieldValue.serverTimestamp()
                    println("DocumentSnapshot written, Timestamp")
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

    private fun resetProperties() {
        queues.clear()
        queue = null
        newQueue = true
        aheadOfUser = 0
        textViewYourNr.text = "0"
        textViewServingNow.text = "0"
        textViewAhead.text = "0"
        textViewEstimate.text = "0"
    }

    private fun zeroLead(input: String): String {
        var zeroLead: StringBuilder = StringBuilder()

        println("INPUT: ${input.length}")
        if (input.length <= 3) {
            for (i in 3 downTo input.length - 1) {
                zeroLead.append("0")
            }
        }
        println("zeroLead: $zeroLead")
        return zeroLead.toString()
    }
}

