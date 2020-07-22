package com.shafigh.easyq.modules

class Constants {
    companion object {
        const val MAP_API: String = com.shafigh.easyq.BuildConfig.MAP_API
        const val POI_COLLECTION: String = "placeOfInterest"
        const val QUEUE_OPTION_COLLECTION: String = "queueOptions"
        const val QUEUE_COLLECTION: String = "queue"
        const val USER_SHARED_PREF_NAME: String = "userPref"
        const val USER_UID:String = "userUid"
        const val CHANNEL_ID: String = "com.shafigh.easyq.notifications"
        const val ACTIVE_Q_CHANNEL: String = "com.shafigh.easyq.active.q"
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
        const val REQUEST_CHECK_SETTINGS = 2
        const val PLACE_PICKER_REQUEST = 3
        const val WIFI_STATE_PERMISSION_CODE = 4
        const val INTERNET_PERMISSION_CODE = 5
        const val LAT_LANG = "lat_lang"
    }
}