package com.shafigh.easyq.modules

import com.google.firebase.firestore.Exclude
import java.io.Serializable
import java.util.*

data class QueueOptions(
    var name: String = "Default",
    @get:Exclude var queueDocId: String = "",
    @get:Exclude var placeDocId: String = ""
) : Serializable {
}