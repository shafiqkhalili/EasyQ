package com.shafigh.easyq.modules

object DataManager {
    private val queues = mutableListOf<Queue>()
    private val queueTypes = mutableListOf<QueueTypes>()

    init {
        setQueueTypes()
    }

    fun getQueueTypes(): MutableList<QueueTypes> {
        return queueTypes;
    }
    private fun setQueueTypes(){
        var qType = QueueTypes("Beard",5,1,30)
        queueTypes.add(qType)
        qType = QueueTypes("Hair",13,4,45)
        queueTypes.add(qType)
    }

    fun queues(){

    }
}