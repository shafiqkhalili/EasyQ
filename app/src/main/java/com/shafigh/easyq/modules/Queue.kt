package com.shafigh.easyq.modules

import com.google.firebase.firestore.Exclude
import java.io.Serializable


data class Queue(
    var done: Boolean = false,
    var issuedAt: Any = "",
    @get:Exclude var uid:String? = null
) : Serializable {

}