package com.shafigh.easyq.modules

import java.util.*

object DataManager {

    private var queueOptions = mutableListOf<QueueOptions>()
    private val queues = mutableListOf<Queue>()
    init {

    }
    fun getQueueOptions(): MutableList<QueueOptions> {
        return queueOptions;
    }

    fun setQueueOptions(queueOpt: QueueOptions){
        queueOptions.add(queueOpt)
    }
    fun queueOptionsSize(): Int {
        return queueOptions.size
    }
    fun resetQueueOptions(): Unit {
        queueOptions.clear()
    }
    fun setQueues(queue: Queue): Unit {
        queues.add(queue)
    }
    fun getQueues(): MutableList<Queue> {
        return queues
    }

}