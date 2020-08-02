package com.shafigh.easyq.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.*


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var currentUser: FirebaseUser? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var softInputAssist: SoftInputAssist

    var poiObject: PlaceOfInterest? = null
    private lateinit var username: TextView
    private lateinit var password: EditText
    private lateinit var login: Button
    private lateinit var signup: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser
        db = FirebaseFirestore.getInstance()

        softInputAssist = SoftInputAssist(this)

        username = findViewById<EditText>(R.id.username)
        password = findViewById<EditText>(R.id.password)
        login = findViewById<Button>(R.id.login)
        signup = findViewById<Button>(R.id.singup)


        login.isEnabled = true
        currentUser?.let { user ->
            if (!user.isAnonymous) {
                login.visibility = View.GONE
                signup.visibility = View.GONE
            } else {
                login.visibility = View.VISIBLE
                signup.visibility = View.VISIBLE
            }
            if (DataManager.placeId == null) {
                signup.visibility = View.GONE
            }
            println("placeid: " + DataManager.placeId)
            println("userName: ${user.uid}")

        }

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        signup.setOnClickListener {
            if (!poiHasUser() && currentUser?.isAnonymous!!) {
                DataManager.placeId?.let { placeId ->
                    db.collection(Constants.POI_COLLECTION).document(placeId)
                        .get().addOnSuccessListener { document ->
                            if (!document.contains(Constants.USER_UID)) {
                                //document.getString("userUid")
                                if (validateInputs(
                                        username.text.toString(),
                                        password.text.toString()
                                    )
                                ) {
                                    //Convert Anonymous to Email login
                                    currentUser?.let { usr ->
                                        val credential = EmailAuthProvider.getCredential(
                                            username.text.toString(),
                                            password.text.toString()
                                        )
                                        auth.currentUser!!.linkWithCredential(credential)
                                            .addOnCompleteListener(this) { task ->
                                                if (task.isSuccessful) {
                                                    println("linkWithCredential:success")
                                                    currentUser = task.result?.user

                                                    println("userID: ${currentUser?.uid}")
                                                    println("DataManager.placeId: ${DataManager.placeId}")
                                                    poiObject = PlaceOfInterest(usr.uid)
                                                    db.collection(Constants.POI_COLLECTION)
                                                        .document(placeId)
                                                        .set(poiObject!!).addOnSuccessListener {
                                                            val user =
                                                                User(
                                                                    currentUser?.uid,
                                                                    true,
                                                                    DataManager.placeId
                                                                )
                                                            DataManager.inloggedUser = user
                                                            println("Datamanager : ${DataManager.inloggedUser}")
                                                            val intent =
                                                                Intent(
                                                                    this,
                                                                    AdminActivity::class.java
                                                                )
                                                            startActivity(intent)
                                                            return@addOnSuccessListener
                                                        }
                                                        .addOnFailureListener { e ->
                                                            userDelete()
                                                            println("Error writing user to poi " + e.localizedMessage)
                                                        }

                                                } else {
                                                    println("linkWithCredential:failure: " + task.exception)
                                                    Toast.makeText(
                                                        baseContext, "Authentication failed.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }

                                        /*auth.createUserWithEmailAndPassword(
                                            username,
                                            password
                                        ).addOnSuccessListener {
                                            currentUser = auth.currentUser
                                            println("userID: ${currentUser?.uid}")
                                            println("DataManager.placeId: ${DataManager.placeId}")
                                            poiObject = PlaceOfInterest(usr.uid)
                                            db.collection(Constants.POI_COLLECTION)
                                                .document(placeId)
                                                .set(poiObject!!).addOnSuccessListener {
                                                    val user =
                                                        User(
                                                            currentUser?.uid,
                                                            true,
                                                            DataManager.placeId
                                                        )
                                                    DataManager.inloggedUser = user
                                                    println("Datamanager : ${DataManager.inloggedUser}")
                                                    val intent =
                                                        Intent(this, AdminActivity::class.java)
                                                    startActivity(intent)
                                                    return@addOnSuccessListener
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
                                        }*/
                                    }
                                }

                            } else {
                                println("Error getting documents: , exception")
                                Toast.makeText(
                                    this,
                                    "Error getting documents: , exception",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            println("Error getting documents: , $exception")
                            Toast.makeText(
                                this,
                                "Error getting documents: , exception",
                                Toast.LENGTH_LONG
                            )
                                .show()
                        }
                }
            } else {
                Toast.makeText(this, "POI has already user", Toast.LENGTH_LONG)
                    .show()
                return@setOnClickListener
            }
        }

        login.setOnClickListener {

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
                            poiObject = PlaceOfInterest(usr.uid)
                            println("Auth: ${usr.uid}")
                            //Get POI of inlogged user
                            db.collection(Constants.POI_COLLECTION).whereEqualTo(Constants.USER_UID, usr.uid)
                                .get().addOnSuccessListener { documents ->
                                    if (documents != null) {
                                        for (doc in documents) {
                                            println("${doc.id} => ${doc.data}")
                                            val poiUser = doc.getString(Constants.USER_UID)
                                            println("poiUser: $poiUser")
                                            if (poiUser != null && poiUser != "") {
                                                if (poiUser == usr.uid) {
                                                    val user =
                                                        User(
                                                            currentUser?.uid,
                                                            true,
                                                            DataManager.placeId
                                                        )
                                                    DataManager.inloggedUser = user
                                                    println("Datamanager : ${DataManager.inloggedUser}")
                                                    val intent = Intent(
                                                        this,
                                                        AdminActivity::class.java
                                                    )
                                                    startActivity(intent)
                                                } else {
                                                    Toast.makeText(
                                                        this,
                                                        "Not authorized at this place",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            else{
                                                println("userUid not found")
                                            }
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    println("Error getting documents: ${exception.localizedMessage}")
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

    override fun onPause() {
        super.onPause()
        DataManager.placeId = null
        softInputAssist.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        softInputAssist.onDestroy()
    }
    override fun onResume() {
        super.onResume()
        softInputAssist.onResume()
    }
    private fun poiHasUser(): Boolean {
        var poiHasUser: Boolean = false
        DataManager.placeId?.let {
            db.collection(Constants.POI_COLLECTION).document(it)
                .get().addOnSuccessListener { document ->
                    if (document.exists()) {
                        val poiUser = document.getString(Constants.USER_UID)
                        if (poiUser != null && poiUser.isNotBlank()) {
                            poiHasUser = true
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error getting documents:  $exception")
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
        println(domain)
        val emailDomain = domain.substringBeforeLast(".")
        println(emailDomain)
        val website = DataManager.poiWebsite
        website?.let {
            val web = website.substringAfterLast("www.")
            val webDomain = web.substringBefore(".")
            println("Email Domain: $emailDomain")
            println("Web Domain $webDomain")
            if (emailDomain != webDomain) {
                Toast.makeText(
                    this,
                    "You can only login with $webDomain domain emails!",
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
        return false
    }

    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}

