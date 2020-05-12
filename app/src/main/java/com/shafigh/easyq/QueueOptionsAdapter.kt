package com.shafigh.easyq

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.shafigh.easyq.activities.ActiveQueueActivity
import com.shafigh.easyq.modules.QueueOptions
import java.lang.Exception
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class QueueOptionsAdapter(val context: Context, private val queueOptions: MutableList<QueueOptions>) :
    RecyclerView.Adapter<QueueOptionsAdapter.ViewHolder>() {

    private val layoutInflater = LayoutInflater.from(context)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        println("From onCreateViewHolder")

        val itemView = layoutInflater.inflate(R.layout.queue_option_item, parent, false)
        return ViewHolder(itemView)
    }

    // hur många views ska recyclerviewn innehålla? så många som finns i persons!
    override fun getItemCount() = queueOptions.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        println("From onBindViewHolder")
        holder.queueOption = queueOptions[position]

        holder.textNextNr.text = "1"
        holder.btnTakeNr.text = holder.queueOption!!.name
        holder.textServingNow.text = "0"
        holder.queueDocId = holder.queueOption!!.queueDocId
        holder.placeDocId = holder.queueOption!!.placeDocId
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textNextNr: TextView = itemView.findViewById(R.id.textViewNextNr)
        var btnTakeNr: Button = itemView.findViewById(R.id.buttonTakeNr)
        var textServingNow: TextView = itemView.findViewById(R.id.textViewServingNow)
        var queueOption: QueueOptions? = null
        var queueDocId: String = ""
        var placeDocId: String = ""
        init {
            println("From Adapter ViewHolder")
            btnTakeNr.setOnClickListener {
                try {
                    val current = LocalDateTime.now()
                    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                    val formattedDate = current.format(formatter)

                    val intent = Intent(context, ActiveQueueActivity::class.java)
                    intent.putExtra("QUEUE_OPTION", queueOption)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                }catch (e:Exception){
                    println("Line 68 ${e.localizedMessage}")
                }
            }
        }
    }

}