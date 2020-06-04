package com.shafigh.easyq.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.Constants
import com.shafigh.easyq.modules.DataManager
import com.shafigh.easyq.modules.PlaceOfInterest
import com.shafigh.easyq.modules.User


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore

    var poi: PlaceOfInterest? = null
    private lateinit var username: TextView
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var logout: Button
    private lateinit var signup: Button
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        db = FirebaseFirestore.getInstance()

        username = findViewById<EditText>(R.id.username)
        password = findViewById<EditText>(R.id.password)
        login = findViewById<Button>(R.id.login)
        signup = findViewById<Button>(R.id.singup)
        logout = findViewById<Button>(R.id.logout)
        loading = findViewById<ProgressBar>(R.id.loading)
        login.isEnabled = true
        currentUser?.let { user ->
            if (!user.isAnonymous) {
                logout.visibility = View.VISIBLE
                login.visibility = View.GONE
                signup.visibility = View.GONE
            } else {
                logout.visibility = View.GONE
                login.visibility = View.VISIBLE
                signup.visibility = View.VISIBLE
            }
            if (DataManager.placeId == null) {
                signup.visibility = View.GONE
            }
            println("placeid: " + DataManager.placeId)
            println("userName: ${user.uid}")

        }
        signup.setOnClickListener {
            if (poiHasUser() && currentUser?.isAnonymous!!) {
                Toast.makeText(this, "POI has already user", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
            DataManager.placeId?.let { placeId ->
                db.collection(Constants.POI_COLLECTION).document(placeId)
                    .get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val poiUser = document.getString("userUid")
                            if (poiUser != null && poiUser.isNotBlank()) {
                                Toast.makeText(
                                    this,
                                    "This place has already a user",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            } else {
                                if (validateInputs(
                                        username.text.toString(),
                                        password.text.toString()
                                    )
                                ) {
                                    currentUser?.let { usr ->
                                        auth.createUserWithEmailAndPassword(
                                            username.text.toString(),
                                            password.text.toString()
                                        ).addOnSuccessListener {
                                            currentUser = auth.currentUser
                                            println("userID: ${currentUser?.uid}")
                                            println("DataManager.placeId: ${DataManager.placeId}")
                                            poi = PlaceOfInterest(usr.uid)
                                            db.collection(Constants.POI_COLLECTION)
                                                .document(placeId)
                                                .set(poi!!).addOnSuccessListener {
                                                    val user =
                                                        User(
                                                            currentUser?.uid,
                                                            true,
                                                            DataManager.placeId
                                                        )
                                                    DataManager.inloggedUser = user
                                                    val intent =
                                                        Intent(this, AdminActivity::class.java)
                                                    startActivity(intent)
                                                }
                                                .addOnFailureListener { e ->
                                                    userDelete()
                                                    println("Error writing user to poi " + e.localizedMessage)
                                                }
                                        }.addOnFailureListener {
                                            Toast.makeText(
                                                this,
                                                it.localizedMessage,
                                                Toast.LENGTH_LONG
                                            )
                                                .show()
                                        }
                                    }

                                }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.w("TAG", "Error getting documents: ", exception)
                    }
            }
        }

        login.setOnClickListener {
            loading.visibility = View.VISIBLE
            if (validateInputs(username.text.toString(), password.text.toString())) {
                //Login by email and password
                auth.signInWithEmailAndPassword(
                    username.text.toString(),
                    password.text.toString()
                ).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        println("signInWithEmail:success")
                        currentUser = auth.currentUser
                        currentUser?.let { usr ->
                            poi = PlaceOfInterest(usr.uid)

                            //Get POI of inlogged user
                            db.collection(Constants.POI_COLLECTION).whereEqualTo("userUid", usr.uid)
                                .get().addOnSuccessListener { documents ->
                                    if (documents != null) {
                                        for (doc in documents) {
                                            val poiUser = doc.getString("userUid")
                                            if (poiUser != null && poiUser.isNotBlank()) {
                                                if (poiUser == currentUser!!.uid) {
                                                    val user =
                                                        User(
                                                            currentUser?.uid,
                                                            true,
                                                            DataManager.placeId
                                                        )
                                                    DataManager.inloggedUser = user
                                                    val intent = Intent(
                                                        this,
                                                        AdminActivity::class.java
                                                    )
                                                    startActivity(intent)
                                                } else {
                                                    logOut()
                                                    Toast.makeText(
                                                        this,
                                                        "Not authorized at this place",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.w(
                                        "TAG",
                                        "Error getting documents: ",
                                        exception
                                    )
                                }
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        println("signInWithEmail:failure: " + task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed: ${task.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            /* auth.fetchSignInMethodsForEmail(username.text.toString())
                 .addOnSuccessListener { result ->
                     val signInMethods = result.signInMethods!!
                     //If user  exists in Authenticaiton, sign in
                     if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {

                     } else {
                         Toast.makeText(this, "User not exist!", Toast.LENGTH_LONG)
                             .show()
                     }
                 }.addOnFailureListener {
                     println(it.localizedMessage)
                 }*/
        }
        loading.visibility = View.GONE


        logout.setOnClickListener {
            logOut()
        }

        val navigation = findViewById<View>(R.id.bottom_nav) as BottomNavigationView
        navigation.selectedItemId = R.id.nav_admin
        DataManager.inloggedUser?.let { user ->
            if (user.isBusiness) {
                navigation.menu.removeItem(R.id.nav_active_queue)
                navigation.menu.removeItem(R.id.nav_home)
            }
        }
        navigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val map = Intent(this, MapsActivity::class.java)
                    startActivity(map)
                }
                R.id.nav_active_queue -> {
                    if (DataManager.hasActiveQueue) {
                        val active = Intent(this, ActiveQueueActivity::class.java)
                        startActivity(active)
                    } else {
                        Toast.makeText(this, "You don't have active queue", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                /*R.id.nav_account -> {
                    val b = Intent(this, LoginActivity::class.java)
                    startActivity(b)
                }*/
            }
            false
        }
    }

    private fun poiHasUser(): Boolean {
        var poiHasUser: Boolean = false
        DataManager.placeId?.let {
            db.collection(Constants.POI_COLLECTION).document(it)
                .get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val poiUser = document.getString("userUid")
                        if (poiUser != null && poiUser.isNotBlank()) {
                            poiHasUser = true
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("TAG", "Error getting documents: ", exception)
                }
        }
        return poiHasUser
    }

    private fun signInAuth(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    println("signInWithEmail:success")
                    currentUser = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    println("signInWithEmail:failure: " + task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun createUserAuth(email: String, password: String): Unit {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                println("createUserWithEmail:success")
                currentUser = auth.currentUser
            } else {
                println(task.exception?.localizedMessage)
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        val domain = email.substringAfterLast("@")
        val emailDomain = domain.substringBeforeLast(".")

        val website = DataManager.poiWebsite
        website?.let {
            val web = website.substringAfterLast("www.")
            val webDomain = web.substringBeforeLast(".")

            if (emailDomain != webDomain) {
                Toast.makeText(
                    this,
                    "You can only login with an official email !",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            if (password.length <= 5) {
                Toast.makeText(
                    this,
                    "Password must at least 5 characters",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            println("email: $emailDomain, web: $webDomain")
        }
        return true
    }

    private fun logOut(): Unit {
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

    private fun userDelete(): Unit {
        if (!currentUser?.isAnonymous!!) {
            currentUser!!.delete()
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            /*R.id.nav_home -> {
                val map = Intent(this, MapsActivity::class.java)
                startActivity(map)
            }
            R.id.nav_business_login -> {
                if (auth.currentUser == null) {
                    val active = Intent(this, LoginActivity::class.java)
                    startActivity(active)
                } else {
                    Toast.makeText(this, "You are already logged in", Toast.LENGTH_SHORT)
                        .show()
                }
            }*/
            R.id.nav_settings -> {
                Toast.makeText(this, "Settings licked", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return false
    }
}

