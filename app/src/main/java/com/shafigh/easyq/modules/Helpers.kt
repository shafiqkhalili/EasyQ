package com.shafigh.easyq.modules

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.Constants.Companion.LOCATION_PERMISSION_REQUEST_CODE
import com.shafigh.easyq.modules.Constants.Companion.WIFI_STATE_PERMISSION_CODE


object Helpers {
    //Create uuid string in Shared Pref. or get if already created

    fun checkPermissions(context: Context, activity: Activity) {

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            //if not granted, ask for permission
            ActivityCompat.requestPermissions(
                activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_WIFI_STATE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            //if not granted, ask for permission
            ActivityCompat.requestPermissions(
                activity, arrayOf(android.Manifest.permission.ACCESS_WIFI_STATE),
                WIFI_STATE_PERMISSION_CODE
            )
        }
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.INTERNET
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            //if not granted, ask for permission
            ActivityCompat.requestPermissions(
                activity, arrayOf(android.Manifest.permission.INTERNET),
                WIFI_STATE_PERMISSION_CODE
            )
        }
    }

    fun getUidFromSharedPref(context: Context): String {

        val pref: SharedPreferences =
            context.getSharedPreferences(
                Constants.USER_SHARED_PREF_NAME,
                Context.MODE_PRIVATE
            )
        var token: String = "null"
        if (pref.contains(R.string.auth_token.toString())) {
            token = pref.getString(R.string.auth_token.toString(), "").toString()
        }
        return token
    }

    fun setUidInSharedPref(token: String, context: Context) {
        val pref: SharedPreferences =
            context.getSharedPreferences(
                Constants.USER_SHARED_PREF_NAME,
                Context.MODE_PRIVATE
            )
        var oldToken: String = ""
        if (pref.contains(R.string.auth_token.toString())) {
            oldToken = pref.getString(R.string.auth_token.toString(), "").toString()
        }
        if (token != oldToken) {
            val editor: SharedPreferences.Editor = pref.edit()
            editor.putString(R.string.auth_token.toString(), token)
            editor.apply()
        }
    }
}