package com.shafigh.easyq.adapters

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.Constants
import com.shafigh.easyq.modules.QueueOptions
import java.util.*


class AdminAdapter(
    val context: Context,
    private val queueOptions: MutableList<QueueOptions>
) : RecyclerView.Adapter<AdminAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = layoutInflater.inflate(R.layout.admin_items, parent, false)
        return ViewHolder(itemView)
    }

    // hur många views ska recyclerviewn innehålla? så många som finns i persons!
    override fun getItemCount() = queueOptions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.textAvailableNr.text = queueOptions[position].availableNr.toString().padStart(3, '0')
        holder.btnNextNr.text = queueOptions[position].name
        holder.textServingNow.text = queueOptions[position].servingNow.toString().padStart(3, '0')
        //holder.textAverageTime.text = queueOptions[position].averageTime.toString().padStart(3, '0')
        holder.btnNextNr.setOnClickListener {
            try {
                println("vdfdfödhfdlf")
                val queueOpt = queueOptions[position]
                nextQ(queueOpt)

            } catch (e: Exception) {
                println("Error on intent: ${e.localizedMessage}")
            }
        }

    }

    private fun nextQ(qOpt: QueueOptions): Unit {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val db = FirebaseFirestore.getInstance()

        val queueOptCollectionRef =
            qOpt.servingQueueDocId?.let {
                db.collection(Constants.POI_COLLECTION).document(qOpt.poiDocId)
                    .collection(Constants.QUEUE_OPTION_COLLECTION).document(qOpt.queueOptDocId)
                    .collection(Constants.QUEUE_COLLECTION).document(it)
            }
        println("DocIDDDD: ${qOpt.servingQueueDocId}")
        val queue: MutableMap<String, Any> = HashMap()
        queue["done"] = true
        queueOptCollectionRef?.set(queue, SetOptions.merge())?.addOnSuccessListener {
            println("DocumentSnapshot successfully deleted!")
            Toast.makeText(context, "Next Person", Toast.LENGTH_SHORT).show()
        }?.addOnFailureListener { e ->
            Toast.makeText(context, "Next Person Error", Toast.LENGTH_SHORT).show()
            println("Error deleting document: " + e.localizedMessage) }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textAvailableNr: TextView = itemView.findViewById(R.id.textViewNextNr)
        var btnNextNr: Button = itemView.findViewById(R.id.buttonNextNr)
        var textServingNow: TextView = itemView.findViewById(R.id.textViewServingNow)
    }
}