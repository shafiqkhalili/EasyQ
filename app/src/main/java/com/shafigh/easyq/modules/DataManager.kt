package com.shafigh.easyq.modules

object DataManager {

    var queueOptions = mutableListOf<QueueOptions>()
    val queues = mutableListOf<Queue>()
    var queue: Queue? = null
    var queueOption: QueueOptions? = null
    var hasActiveQueue = false
    var placeId: String? = null
    var poiWebsite: String? = null
    var isAdmin: Boolean = false
    var inloggedUser: User? = null
    var takeQueue: Boolean = false
    var bubbleActive: Boolean = false
    var resetQueue: Boolean = false
    /*
    fun setQueue(queue: Queue): Unit {
        this.queue = queue
    }

    fun getQueue(): Queue? {
        return this.queue
    }

    fun getQueueOptions(): MutableList<QueueOptions> {
        return queueOptions;
    }

    fun setQueueOptions(qOpts: MutableList<QueueOptions>) {
        queueOptions = qOpts;
    }
    fun getQueueOption(): QueueOptions? {
        return this.queueOption
    }

    fun hasActiveQueue(): Boolean {
        return hasActiveQueue
    }

    fun queueOptionIsNull(): Boolean {
        return queueOption == null
    }

    fun setQueueOption(queueOpt: QueueOptions? = null) {
        queueOption = queueOpt
    }

    fun getQueueIndex(queue: Queue): Int {
        return this.queues.indexOf(queue)
    }

    fun getServingQueueIndex(): Int {
        return this.queues.indexOfFirst {
            !it.done
        }
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
    }*/

}