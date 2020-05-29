package com.shafigh.easyq.modules

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore


object Firestore {
    val db = FirebaseFirestore.getInstance()
    private val queues = mutableListOf<Queue>()
    private val queueOptions = mutableListOf<QueueOptions>()

    //Add POI to Firestore with it's default QueueOption
    fun initPOI(poiID: String): Unit {
        val poiRef = db.collection(Constants.POI_COLLECTION).document(poiID)
        val queueOptRef = poiRef.collection(Constants.QUEUE_OPTION_COLLECTION)
        val queueOpt = QueueOptions("Default")
        queueOptRef.add(queueOpt)
            .addOnSuccessListener { documentReference ->
                Log.d("TAG", "DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("TAG", "Error adding document", e)
            }
    }

    //Check if POI exists
    fun poiExists(poiDocID:String):Boolean{
        val docRef = db.collection(Constants.POI_COLLECTION).document(poiDocID)
        var exist : Boolean = false
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("FSGET", "DocumentSnapshot data: ${document.data} ")
                    exist = true
                } else {
                    Log.d("FSGET", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("FSGET", "get failed with ", exception)
            }
        return exist
    }
    fun getPOI(poiDocID: String) {
        println("getPOI called")
        val docRef = db.collection(Constants.POI_COLLECTION).document("xVoeoZabfOcivJ3TJy1l")
            .collection(Constants.QUEUE_OPTION_COLLECTION).document("QbNRljFu2p53HV8J5nC5")
        docRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()){
                    println("Doc exist!!!")
                }
                if (document != null) {
                    Log.d("FSGET", "DocumentSnapshot data: ${document.data} ")
                } else {
                    Log.d("FSGET", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("FSGET", "get failed with ", exception)
            }
    }

    //Add queue to Queue list of a POI's queue option document
    fun addQueue(poiDocId: String, queueOptDocId: String,userUuid:String, queue: Queue): Boolean {

        // Add a new document with a generated ID
        db.collection(Constants.POI_COLLECTION).document(poiDocId)
            .collection(Constants.QUEUE_OPTION_COLLECTION).document(queueOptDocId)
            .collection(Constants.QUEUE_COLLECTION).document(userUuid)
            .set(queue)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "Data",
                    "DocumentSnapshot added with ID: " + documentReference
                )
                return@addOnSuccessListener
            }
            .addOnFailureListener { e ->
                Log.w("Data", "Error adding document", e)
                return@addOnFailureListener
            }
        return false
    }

    //Get list of queues based on a POI
    fun readAllQueue(poiDocId: String, queueOptDocId: String) {
        db.collection(Constants.POI_COLLECTION).document(poiDocId)
            .collection(Constants.QUEUE_OPTION_COLLECTION).document(queueOptDocId)
            .collection(Constants.QUEUE_COLLECTION)
            .get()
            .addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    for (document in snapshot.result!!) {
                        Log.d("TAG", document.id + " => " + document.data)
                        val queue = snapshot as Queue
                        DataManager.setQueues(queue)
                    }
                } else {
                    Log.w("TAG", "Error getting documents.", snapshot.exception)
                }
            }
    }

    //Get queue options of a POI
    fun readAllQueueOptions(poiDocId: String): MutableList<QueueOptions> {
        val queueOptions = mutableListOf<QueueOptions>()
        println("Reading queues from firebase")
        db.collection(Constants.POI_COLLECTION).document(poiDocId)
            .collection(Constants.QUEUE_OPTION_COLLECTION)
            .get()
            .addOnCompleteListener { snapshot ->
                if (snapshot.isSuccessful) {
                    for (document in snapshot.result!!) {
                        Log.d("isSuccess", document.id + " => " + document.data)
                        val opt = document.toObject(QueueOptions::class.java)
                        println("Option: ${opt.name}")
                        queueOptions.add(opt)
                    }
                } else {
                    Log.w("isFailure", "Error getting documents.", snapshot.exception)
                }
            }
        println("Reading queues finished, list size: ${queueOptions.size}")
        return queueOptions
    }

    //Add queue option to a POI
    fun addQueueOption(poiDocId: String, queueOptDocId: String, queueOpt: QueueOptions): Boolean {
        db.collection(Constants.POI_COLLECTION).document(poiDocId)
            .collection(Constants.QUEUE_OPTION_COLLECTION)
            .add(queueOpt)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "Data",
                    "DocumentSnapshot added with ID: " + documentReference.id
                )
                return@addOnSuccessListener
            }
            .addOnFailureListener { e ->
                Log.w("Data", "Error adding document", e)
                return@addOnFailureListener
            }
        return false
    }
}
