package com.shafigh.easyq.modules

import android.content.Context
import android.content.SharedPreferences
import com.shafigh.easyq.R


object Helpers {
    //Create uuid string in Shared Pref. or get if already created

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