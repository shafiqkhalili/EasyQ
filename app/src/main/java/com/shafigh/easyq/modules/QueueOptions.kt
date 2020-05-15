package com.shafigh.easyq.modules

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class QueueOptions(
    var name: String = "Default",
    @get:Exclude var queueOptDocId: String = "",
    @get:Exclude var poiDocId: String = ""
) : Serializable {
}