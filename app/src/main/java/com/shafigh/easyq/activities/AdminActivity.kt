package com.shafigh.easyq.activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.DataManager
import com.shafigh.easyq.modules.Queue
import com.shafigh.easyq.modules.QueueOptions


class AdminActivity : AppCompatActivity() {
    private lateinit var textViewHeader: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewDate: TextView
    private lateinit var textViewOptionName: TextView
    private lateinit var textViewYourNr: TextView
    private lateinit var buttonCancel: Button
    private lateinit var textViewEstimate: TextView
    private lateinit var textViewServingNow: TextView
    private lateinit var textViewAhead: TextView

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null

    //Firebase variables
    private var queue: Queue? = null
    private var queueOption: QueueOptions? = null
    private var queues = mutableListOf<Queue>()
    private var servingNow: Int = 0
    private var averageTime: Int = 0
    private var userPosition: Int = 0
    private var usersAhead: Int = 0

    private var existsInDatabase: Boolean = true
    private var queueCollectionRef: CollectionReference? = null

    override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        val toolbar = findViewById<Toolbar>(R.id.toolbar_admin)
        setSupportActionBar(toolbar)

        val navigation = findViewById<View>(R.id.bottom_nav) as BottomNavigationView
        navigation.selectedItemId = R.id.nav_admin

        if (DataManager.inloggedUser == null)
            println("Datamanager is null")
        DataManager.inloggedUser?.let { user ->
            println("isBusiness: " + user.isBusiness)
            if (user.isBusiness) {
                navigation.menu.removeItem(R.id.nav_active_queue)
                navigation.menu.removeItem(R.id.nav_home)
            }
        }
        navigation.defaultFocusHighlightEnabled
        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val map = Intent(this, MapsActivity::class.java)
                    startActivity(map)
                }
                /* R.id.nav_active_queue -> {
                     if (DataManager.hasActiveQueue()) {
                         val active = Intent(this, ActiveQueueActivity::class.java)
                         startActivity(active)
                     } else {
                         Toast.makeText(this, "You don't have active queue", Toast.LENGTH_SHORT)
                             .show()
                     }
                 }
                 R.id.nav_admin -> {
                     if (currentUser != null) {
                         Toast.makeText(this, "Welcom ${currentUser.displayName}", Toast.LENGTH_SHORT)
                             .show()
                     }
                     if (currentUser == null) {
                         val b = Intent(this, LoginActivity::class.java)
                         startActivity(b)
                     }else{
                         val b = Intent(this, AdminActivity::class.java)
                         startActivity(b)
                     }
                 }*/
            }
            false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        println("login clicked")
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
            R.id.nav_settings -> {
                Toast.makeText(this, "Settings licked", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return false
    }

}
