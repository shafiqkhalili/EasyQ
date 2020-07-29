package com.shafigh.easyq.modules


import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import com.shafigh.easyq.R
import com.shafigh.easyq.activities.ActiveQueueActivity
import java.lang.System.currentTimeMillis

@RequiresApi(Build.VERSION_CODES.Q)
class NotificationHelper(private val context: Context) {
    companion object {
        private const val REQUEST_CONTENT = 1
        private const val REQUEST_BUBBLE = 2
        private const val NOTIFICATION_ID = 0
    }

    init {
        setUpNotificationChannels()
    }

    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun setUpNotificationChannels() {
        if (notificationManager?.getNotificationChannel(
                Constants.ACTIVE_Q_CHANNEL
            ) == null
        ) {
            try {
                val channel = NotificationChannel(
                    Constants.ACTIVE_Q_CHANNEL,
                    context.getString(
                        R.string.channel_quotes
                    ),
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    setAllowBubbles(true)
                    description = context.getString(
                        R.string.channel_quotes_description
                    )
                }
                notificationManager?.createNotificationChannel(channel)
            } catch (e: Exception) {
                println(e.localizedMessage)
            }
        }
    }

    fun showNotification(fromUser: Boolean, usersAhead: Int) {

        // Create Icon
        val icon = createIcon()

        // Create the Contact
        val person = createPerson(icon)

        // Create the Notification
        val notification = createNotification(icon, person, usersAhead)

        // Create the Bubble's Metadata
        val bubbleMetaData = createBubbleMetadata(icon, fromUser)

        // Set the bubble metadata
        notification.setBubbleMetadata(bubbleMetaData)

        // Build and Display the Notification
        notificationManager?.notify(NOTIFICATION_ID, notification.build())


    }

    private fun createPerson(icon: Icon): Person {
        return Person.Builder()
            .setName("Active Queue")
            .setIcon(icon)
            .setBot(true)
            .setImportant(true)
            .build()
    }

    private fun createIcon(): Icon {
        return Icon.createWithAdaptiveBitmap(
            BitmapFactory.decodeResource(
                context.resources,
                R.drawable.easyq
            )
        )
    }

    private fun createIntent(requestCode: Int): PendingIntent {
        return PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, ActiveQueueActivity::class.java).apply {
                putExtra("isActiveQueue", true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_ONE_SHOT
        )
    }

    private fun createNotification(icon: Icon, person: Person, qAhead: Int): Notification.Builder {
        return Notification.Builder(context, Constants.ACTIVE_Q_CHANNEL)
            .setContentTitle("Active Queue")
            .setLargeIcon(icon)
            .setSmallIcon(icon)
            .setCategory(Notification.CATEGORY_STATUS)
            .setStyle(
                Notification.MessagingStyle(person)
                    .setGroupConversation(false)
                    .addMessage("You are after $qAhead people", currentTimeMillis(), person)
            )
            .addPerson(person)
            .setShowWhen(true)
            .setContentIntent(createIntent(REQUEST_CONTENT))
    }

    private fun createBubbleMetadata(icon: Icon, fromUser: Boolean):
            Notification.BubbleMetadata {
        return Notification.BubbleMetadata.Builder()
            .setDesiredHeight(600)
            .setIcon(icon)
            .apply {
                if (fromUser) {
                    setAutoExpandBubble(false)
                    setSuppressNotification(true)
                }
            }
            .setIntent(createIntent(REQUEST_BUBBLE))
            .build()
    }

}