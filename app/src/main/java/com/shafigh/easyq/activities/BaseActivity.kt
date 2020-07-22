package com.shafigh.easyq.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.DataManager

class BaseActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        //setSupportActionBar(toolbar)
        // Custom View for ActionBar
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        DataManager.inloggedUser?.let { user ->
            if (user.isBusiness) {
                menu?.removeItem(R.id.nav_active_queue)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }
}