package com.shafigh.easyq.modules

import com.google.android.gms.maps.model.PointOfInterest

class PointOfInterest(var pointOfInterest: PointOfInterest,
                      queueOptions: List<QueueOptions>? = null)
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

