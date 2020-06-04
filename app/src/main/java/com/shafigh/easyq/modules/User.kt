package com.shafigh.easyq.modules

import java.io.Serializable

class User(var userID: String? = null, var isBusiness: Boolean = false, var placeId: String? = null) :
    Serializable {
}