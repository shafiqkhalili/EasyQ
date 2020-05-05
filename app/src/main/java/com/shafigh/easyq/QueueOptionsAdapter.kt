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
import com.shafigh.easyq.modules.DataManager
import com.shafigh.easyq.modules.Queue
import com.shafigh.easyq.modules.QueueTypes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class QueueOptionsAdapter(val context:Context, private val options: List<QueueTypes>):
    RecyclerView.Adapter<QueueOptionsAdapter.ViewHolder>() {

    //inflator behövs för att skapa en view utifrån en layout (xml)
    private val layoutInflater = LayoutInflater.from(context)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //använder vår inflator för att skapa en view
        val itemView = layoutInflater.inflate(R.layout.queue_option_item, parent, false )
        // skapar vi en viewHolder av vår egna klass ViewHolder (skriven längre ner här)
        return ViewHolder(itemView)
    }

    // hur många views ska recyclerviewn innehålla? så många som finns i persons!
    override fun getItemCount() = options.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.option = options[position]

        // sätter in den personens uppgifter i vår view
        holder.textNextNr.text = holder.option!!.availableNr.toString()
        holder.btnTakeNr.text = holder.option!!.queueName
        holder.textServingNow.text = holder.option!!.servingNow.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        // när en viewholder skapas så letar vi reda på våra två textview:s som finns i vår item_view
        val textNextNr: TextView = itemView.findViewById(R.id.textViewNextNr)
        var btnTakeNr: Button = itemView.findViewById(R.id.buttonTakeNr)
        var textServingNow : TextView = itemView.findViewById(R.id.textViewServingNow)
        var option : QueueTypes? = null

        init {
            btnTakeNr.setOnClickListener{
                //Queue
                val current = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                val formattedDate = current.format(formatter)

                val queue = option?.availableNr?.inc()?.let { it1 -> Queue(number = it1, issueTime = current ) }
                if (queue != null) {
                    DataManager.setQueue(queue)
                }
                //Increment next number
                /*if (option?.queueUUID?.isNotEmpty()!!){
                    DataManager.incrementAvailableNr(option!!.queueUUID)
                }*/
                val intent = Intent(context, ActiveQueueActivity::class.java)
                intent.putExtra("PLACE_ID",PLACE_ID)
                intent.putExtra("QUEUE",queue)
                intent.putExtra("QUEUE_OPTION",option)
                context.startActivity(intent)
            }
        }
    }

}