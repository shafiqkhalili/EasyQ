package com.shafigh.easyq.modules

import java.io.Serializable

data class Queue(
    var done: Boolean = false
    //var issuedAt: LocalDateTime = LocalDateTime.now()
    ) : Serializable {

}