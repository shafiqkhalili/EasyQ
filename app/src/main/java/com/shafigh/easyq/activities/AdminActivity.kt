package com.shafigh.easyq.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.shafigh.easyq.R
import com.shafigh.easyq.adapters.AdminAdapter
import com.shafigh.easyq.modules.*
import com.shafigh.easyq.modules.Queue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*


class AdminActivity : AppCompatActivity() {
    private lateinit var textViewHeader: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewDate: TextView

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    private var queues = mutableListOf<Queue>()

    private var queueOptions = mutableListOf<QueueOptions>()
    private var queueOptCollectionRef: CollectionReference? = null

    override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        val db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        val toolbar = findViewById<Toolbar>(R.id.toolbar_admin) as Toolbar
        setSupportActionBar(toolbar)

        val navigation = findViewById<View>(R.id.bottom_nav) as BottomNavigationView
        navigation.selectedItemId = R.id.nav_admin
        navigation.defaultFocusHighlightEnabled
        if (DataManager.inloggedUser == null) {
            println("Datamanager is null")
            val map = Intent(this, MapsActivity::class.java)
            startActivity(map)
            return
        }
        DataManager.inloggedUser?.let { user ->
            println("isBusiness: ${user.placeId}")
            if (user.isBusiness) {
                navigation.menu.removeItem(R.id.nav_active_queue)
                navigation.menu.removeItem(R.id.nav_home)
                toolbar.menu.removeItem(R.id.login)
            }
            user.placeId?.let { placeId ->
                println("LIne 106: $placeId")
                poiInfo(placeId)
                queueOptCollectionRef =
                    db.collection(Constants.POI_COLLECTION).document(placeId)
                        .collection(Constants.QUEUE_OPTION_COLLECTION)
            }
        }

        navigation.defaultFocusHighlightEnabled
        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val map = Intent(this, MapsActivity::class.java)
                    startActivity(map)
                }
            }
            false
        }

        textViewHeader = findViewById(R.id.textViewBusiness)
        textViewAddress = findViewById(R.id.textViewAddress)
        textViewDate = findViewById(R.id.textViewDate)

        var todayDate = Calendar.getInstance()

        todayDate.set(Calendar.HOUR_OF_DAY, 0)
        todayDate.set(Calendar.MINUTE, 0)
        todayDate.set(Calendar.SECOND, 0)
        todayDate.set(Calendar.MILLISECOND, 0)
        val todayMillSecs = todayDate.time

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAdmin)
        recyclerView.layoutManager = LinearLayoutManager(this)

        //db.collection(Constants.POI_COLLECTION).whereEqualTo("userUid",DataManager.inloggedUser.userID.toString())

        println("LIne 111: ")
        queueOptCollectionRef?.let { ref ->
            ref.addSnapshotListener { snapshot, e ->
                println("LIne 114 ")
                if (e != null) {
                    println(e.localizedMessage)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    queueOptions.clear()
                    println("LIne 117")
                    for (document in snapshot.documents) {
                        val queueOpt = document.toObject(QueueOptions::class.java)
                        if (queueOpt != null) {
                            queues.clear()
                            ref.document(document.id)
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
                                    if (latestDone < 0) {
                                        latestDone = 0
                                    }
                                    queueOpt.servingNow = latestDone + 1
                                    queueOpt.availableNr = queues.size + 1
                                    queueOpt.averageTime = queueOpt.averageTime

                                    queueOpt.queueOptDocId = document.id
                                    queueOpt.poiDocId = DataManager.placeId as String
                                    queueOptions.add(queueOpt)
                                    recyclerView.adapter?.notifyDataSetChanged()
                                }.addOnFailureListener{
                                    println(it.localizedMessage)
                                }
                        }
                    }
                } else {
                    println("Current data: null")
                }
                val adapter = AdminAdapter(
                    context = applicationContext,
                    queueOptions = queueOptions
                )
                recyclerView.adapter = adapter
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            /*R.id.nav_home -> {
                val map = Intent(this, MapsActivity::class.java)
                startActivity(map)
            }*/
            R.id.nav_business_login -> {
                println("login clicked")
                try {
                    val active = Intent(this, LoginActivity::class.java)
                    startActivity(active)
                }catch (e:Exception){

                }
            }
            R.id.nav_logout -> {
                logOut()
                val active = Intent(this, MapsActivity::class.java)
                startActivity(active)
            }
        }
        return false
    }

    private fun poiInfo(placeId: String): Unit {
        //Get info about POI from Google API
        Places.initialize(this, Constants.MAP_API)
        println("poiInfo called")
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

    private fun logOut() {
        if (!currentUser?.isAnonymous!!) {
            auth.signOut()
            auth.signInAnonymously()
                .addOnSuccessListener {
                    // Sign in success, update UI with the signed-in user's information
                    currentUser = auth.currentUser
                    DataManager.inloggedUser = User(currentUser?.uid)
                    DataManager.placeId = null
                    println("uid: $currentUser.uid")
                    val active = Intent(this, MapsActivity::class.java)
                    startActivity(active)
                }.addOnFailureListener { task ->
                    // If sign in fails, display a message to the user.
                    println("signInAnonymously:failure " + task.localizedMessage)
                    Toast.makeText(
                        baseContext, "Logged out from business",
                        Toast.LENGTH_SHORT
                    ).show()
                    val map = Intent(this, MapsActivity::class.java)
                    startActivity(map)
                }
        }
    }
}
