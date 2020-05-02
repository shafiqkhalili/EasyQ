package com.shafigh.easyq

import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

class QueueOptionsAdapter(private val context:Context,private val options: List<QueueTypes>): RecyclerView.Adapter<QueueOptionsAdapter.ViewHolder>() {

    //inflator behövs för att skapa en view utifrån en layout (xml)
    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //använder vår inflator för att skapa en view
        val itemView = layoutInflater.inflate(R.layout.queue_option_item, parent, false )
        // skapar vi en viewHolder av vår egna klass ViewHolder (skriven längre ner här)
        return ViewHolder(itemView)
    }

    // hur många views ska recyclerviewn innehålla? så många som finns i persons!
    override fun getItemCount() = options.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //när en item_view ska populeras tar vi rätt person från vår data
        val option = options[position]
        // sätter in den personens uppgifter i vår view
        holder.textViewNextNr.text = option.availableNr.toString()
        holder.buttonOptionName.text = option.queueName.toString()
    }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        // när en viewholder skapas så letar vi reda på våra två textview:s som finns i vår item_view
        val textViewNextNr = itemView.findViewById<TextView>(R.id.textViewNextNr)
        val buttonOptionName = itemView.findViewById<Button>(R.id.buttonTakeNr)
    }
}