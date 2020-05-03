package com.shafigh.easyq.activities

import android.os.Bundle
import android.provider.Contacts
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.Queue
import com.shafigh.easyq.modules.QueueTypes


class ActiveQueueActivity : AppCompatActivity() {

    lateinit var textViewOptionName :TextView
    lateinit var textViewYourNr : TextView
    lateinit var buttonCancel : Button
    lateinit var buttonUseIt : Button
    lateinit var textViewEstimate : TextView
    lateinit var textViewServingNow : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_queue)

        textViewOptionName = findViewById(R.id.textViewOptionName)
        textViewYourNr = findViewById(R.id.textViewYourNr)
        buttonCancel = findViewById(R.id.buttonCancel)
        buttonUseIt = findViewById(R.id.buttonUseNr)
        textViewEstimate = findViewById(R.id.textViewEstimatedTime)
        textViewServingNow = findViewById(R.id.textViewServingNow)

        val queue = intent.getSerializableExtra("QUEUE") as? Queue
        var queueTypes = intent.getSerializableExtra("QUEUE_OPTION") as? QueueTypes

        if (queueTypes != null) {
            textViewOptionName.text = queueTypes.queueName.toString()
        }
        if (queue != null) {
            textViewYourNr.text = queue.number.toString()
        }
        if (queueTypes != null) {
            textViewEstimate.text = queueTypes.AverageEstimated.toString()
        }
        if (queueTypes != null) {
            textViewServingNow.text = queueTypes.servingNow.toString()
        }
    }
}
