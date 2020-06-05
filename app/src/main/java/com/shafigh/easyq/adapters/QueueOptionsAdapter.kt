package com.shafigh.easyq.adapters

import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.shafigh.easyq.R
import com.shafigh.easyq.activities.ActiveQueueActivity
import com.shafigh.easyq.modules.QueueOptions


class QueueOptionsAdapter(
    val context: Context,
    private val queueOptions: MutableList<QueueOptions>
) :
    RecyclerView.Adapter<QueueOptionsAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = layoutInflater.inflate(R.layout.queue_option_item, parent, false)
        return ViewHolder(itemView)
    }

    // hur många views ska recyclerviewn innehålla? så många som finns i persons!
    override fun getItemCount() = queueOptions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.textAvailableNr.text = queueOptions[position].availableNr.toString().padStart(3, '0')
        holder.btnTakeNr.text = queueOptions[position].name
        holder.textServingNow.text = queueOptions[position].servingNow.toString().padStart(3, '0')
        //holder.textAverageTime.text = queueOptions[position].averageTime.toString().padStart(3, '0')
        holder.btnTakeNr.setOnClickListener {
            try {
                val intent = Intent(context, ActiveQueueActivity::class.java)
                intent.putExtra(R.string.QUEUE_OPTIONS_OBJ.toString(), queueOptions[position])
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

            } catch (e: Exception) {
                println("Error on intent: ${e.localizedMessage}")
            }
        }
        holder.itemView.setOnClickListener {
            try {
                val intent = Intent(context, ActiveQueueActivity::class.java)
                intent.putExtra(R.string.QUEUE_OPTIONS_OBJ.toString(), queueOptions[position])
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)

            } catch (e: Exception) {
                println("Error on intent: ${e.localizedMessage}")
            }
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textAvailableNr: TextView = itemView.findViewById(R.id.textViewNextNr)
        var btnTakeNr: Button = itemView.findViewById(R.id.buttonTakeNr)
        var textServingNow: TextView = itemView.findViewById(R.id.textViewServingNow)
    }
}