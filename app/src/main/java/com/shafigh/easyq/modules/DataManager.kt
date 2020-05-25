package com.shafigh.easyq.modules

object DataManager {

    private var queueOptions = mutableListOf<QueueOptions>()
    private val queues = mutableListOf<Queue>()
    private var queue: Queue? = null
    private var queueOption: QueueOptions? = null

    init {

    }

    fun setQueue(queue: Queue): Unit {
        this.queue = queue
    }

    fun getQueue(): Queue? {
        return this.queue
    }

    fun getQueueOptions(): MutableList<QueueOptions> {
        return queueOptions;
    }

    fun getQueueOption(): QueueOptions? {
        return this.queueOption
    }
    fun setQueueOptions(queueOpt: QueueOptions) {
        queueOptions.add(queueOpt)
    }
    fun setQueueOption(queueOpt: QueueOptions) {
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
    }

}