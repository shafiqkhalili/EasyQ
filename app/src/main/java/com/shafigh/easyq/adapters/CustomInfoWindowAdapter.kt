package com.shafigh.easyq.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.shafigh.easyq.R


class CustomInfoWindowAdapter(context: Context?) : GoogleMap.InfoWindowAdapter {

    var mWindow: View? =
        LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)


    private fun renderWindowText(marker: Marker, view: View?) {
        val title = marker.title
        val tvTitle = view!!.findViewById<View>(R.id.title) as TextView
        if (title != "") {
            tvTitle.text = title
        }
        val snippet = marker.snippet
        val tvSnippet = view.findViewById<View>(R.id.snippet) as TextView

        tvSnippet.text = snippet

        if (tvSnippet.text.isEmpty()){
            tvSnippet.visibility = GONE
        }
        println("Snippet: ${marker.snippet}")
    }

    override fun getInfoWindow(marker: Marker): View? {
        renderWindowText(marker, mWindow)
        return mWindow
    }

    override fun getInfoContents(marker: Marker): View? {
        renderWindowText(marker, mWindow)
        return mWindow
    }
}