package com.shafigh.easyq.modules

import java.io.Serializable
import java.sql.Time
import java.time.LocalDateTime
import java.util.*

class Queue(
    var number: Int,
    var issueTime: LocalDateTime,
    var waitTime:Time ? = null,
    var doneTime:Time? = null,
    var uuid: String = UUID.randomUUID().toString()) : Serializable {
}