package com.shafigh.easyq.modules

import java.util.*

object DataManager {
    private lateinit var userQueue : Queue
    private val queueTypes = mutableListOf<QueueTypes>()

    init {
        setQueueTypes()
    }

    fun incrementAvailableNr(queueID: String) {
        for (qt in 0..queueTypes.size){
            if (queueID == queueTypes[qt].queueUUID){
                queueTypes[qt].availableNr.inc()
            }
        }
    }
    fun decrementAvailableNr(queueID: String) {
        for (qt in 0..queueTypes.size){
            if (queueID == queueTypes[qt].queueUUID){
                queueTypes[qt].availableNr.dec()
            }
        }
    }
    fun getQueueTypes(): MutableList<QueueTypes> {
        return queueTypes;
    }

    private fun setQueueTypes(){
        var qType = QueueTypes(UUID.randomUUID().toString(),"Default",0,1,30)
        qType = QueueTypes(UUID.randomUUID().toString(),"Beard",5,1,30)
        queueTypes.add(qType)
        qType = QueueTypes(UUID.randomUUID().toString(),"Hair",13,4,45)
        queueTypes.add(qType)
    }
    fun setQueue(queue: Queue): Unit {
        userQueue = queue
    }
    fun getQueue(): Queue {
        return this.userQueue
    }
}