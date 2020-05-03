package com.shafigh.easyq.modules

import java.io.Serializable

class QueueTypes(
    var queueName: String,
    var availableNr: Int,
    var servingNow: Int,
    var AverageEstimated:Int,
    var Queues: List<Queue>? = null ):Serializable {

}