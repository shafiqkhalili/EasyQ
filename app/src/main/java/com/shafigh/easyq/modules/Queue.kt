package com.shafigh.easyq.modules

import android.os.Build
import androidx.annotation.RequiresApi
import java.io.Serializable
import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.*

data class Queue(
    var done: Boolean = false,
    var issuedAt: LocalDateTime = LocalDateTime.now(),
    var waitedUntil:LocalDateTime ? = null,
    var finishedTime:LocalDateTime? = null,
    var uuid: String = UUID.randomUUID().toString()) : Serializable {

}