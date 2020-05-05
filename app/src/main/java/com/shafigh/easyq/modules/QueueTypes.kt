package com.shafigh.easyq.modules

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class QueueTypes(
    var queueUUID: String,
    var queueName: String,
    var availableNr: Int,
    var servingNow: Int,
    var AverageEstimated:Int):Serializable {

}