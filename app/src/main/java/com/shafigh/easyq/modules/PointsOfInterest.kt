package com.shafigh.easyq.modules

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import java.util.*

class PointOfInterest(var pointOfInterest: PointOfInterest,
                      queueTypes: List<QueueTypes>? = null)
{
    companion object Factory {
        private val poiList = mutableListOf<PointOfInterest>()


        fun addPOI(pointOfInterest: PointOfInterest) {
            poiList.add(pointOfInterest)
        }
        fun getAllPoi(): MutableList<PointOfInterest> {
            return poiList
        }
    }
}
