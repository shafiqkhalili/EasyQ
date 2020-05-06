package com.shafigh.easyq.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Contacts
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.shafigh.easyq.PLACE_API
import com.shafigh.easyq.PLACE_ID
import com.shafigh.easyq.QueueOptionsActivity
import com.shafigh.easyq.R
import com.shafigh.easyq.modules.Queue
import com.shafigh.easyq.modules.QueueTypes
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class ActiveQueueActivity : AppCompatActivity() {

    lateinit var textViewHeader : TextView
    lateinit var textViewAddress: TextView
    lateinit var textViewDate : TextView

    lateinit var textViewOptionName :TextView
    lateinit var textViewYourNr : TextView
    lateinit var buttonCancel : Button
    lateinit var buttonUseIt : Button
    lateinit var textViewEstimate : TextView
    lateinit var textViewServingNow : TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_active_queue)

        placeInfo()
        //Places info
        textViewHeader = findViewById(R.id.textViewBusiness)
        textViewAddress = findViewById(R.id.textViewAddress)
        textViewDate = findViewById(R.id.textViewDate)
        //queue info
        textViewOptionName = findViewById(R.id.textViewOptionName)
        textViewYourNr = findViewById(R.id.textViewYourNr)
        buttonCancel = findViewById(R.id.buttonCancel)
        textViewEstimate = findViewById(R.id.textViewEstimatedTime)
        textViewServingNow = findViewById(R.id.textViewServingNow)

        val queue = intent.getSerializableExtra("QUEUE") as? Queue
        val queueTypes = intent.getSerializableExtra("QUEUE_OPTION") as? QueueTypes

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
        buttonCancel.setOnClickListener{
            val intent = Intent(this, QueueOptionsActivity::class.java)

            intent.putExtra("PLACE_ID", PLACE_ID)
            this.startActivity(intent)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun placeInfo(){
        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
        val formattedDate = current.format(formatter)

        // Initialize Places.
        Places.initialize(applicationContext, PLACE_API)
        // Create a new Places client instance.
        val placesClient = Places.createClient(this)
        // Specify the fields to return.
        val placeFields: List<Place.Field> =
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.OPENING_HOURS,
                Place.Field.PHONE_NUMBER,
                Place.Field.PRICE_LEVEL
            )
        // Construct a request object, passing the place ID and fields array.
        val request = FetchPlaceRequest.newInstance(PLACE_ID, placeFields)

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place: Place = response.place
            Log.i("DEMO", "Place found: " + place.address)
            textViewHeader.text = place.name
            textViewAddress.text = place.address
            textViewDate.text = formattedDate.toString()

        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                val statusCode = exception.statusCode
                // Handle error with given status code.
                Log.e("DEMO","Place not found: " + exception.localizedMessage)
            }
        }
    }
}
