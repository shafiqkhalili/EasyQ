package com.shafigh.easyq.modules

import java.io.Serializable
import java.sql.Time
import java.util.*
import kotlin.collections.ArrayList

class QueueTypes(
    var queueUUID: String,
    var queueName: String,
    var availableNr: Int,
    var servingNow: Int,
    var averageEstimated: Int
) : Serializable {

    companion object {
        private val queues = mutableListOf<QueueTypes>()

        private var availableNumber: Int = 1
        private var servingNow: Int = 0
        private var averageEstimated: Int = 30

        init {
            val defaultQ = QueueTypes(
                UUID.randomUUID().toString(), "Default", availableNumber, servingNow,
                averageEstimated
            )
            queues.add(defaultQ)
        }

        fun newQueue(
            queueUUID: String,
            queueName: String,
            availableNr: Int,
            servingNow: Int,
            AverageEstimated: Int
        ): QueueTypes {
            val queue = QueueTypes(queueUUID, queueName, availableNr, servingNow, AverageEstimated)
            queues.add(queue)
            return queue
        }

    }
}