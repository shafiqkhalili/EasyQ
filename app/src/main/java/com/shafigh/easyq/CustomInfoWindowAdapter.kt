package com.shafigh.easyq

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.shafigh.easyq.R


class CustomInfoWindowAdapter: GoogleMap.InfoWindowAdapter {
    private var mWindow: View? = null
    private var mContext: Context? = null

    fun CustomInfoWindowAdapter(context: Context?) {
        mContext = context
        mWindow = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
    }

    private fun renderWindowText(marker: Marker, view: View?) {
        val title = marker.title
        val tvTitle = view!!.findViewById<View>(R.id.title) as TextView
        if (title != "") {
            tvTitle.text = title
        }
        val snippet = marker.snippet
        val tvSnippet = view.findViewById<View>(R.id.snippet) as TextView
        if (snippet != "") {
            tvSnippet.text = snippet
        }
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