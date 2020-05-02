package com.shafigh.easyq

import java.sql.Time

class Queue(var number:Int, var issueTime:Time, var waitTime:Time ? = null, var doneTime:Time? = null) {
}