package com.lostbearlabs.logstory.log

import com.lostbearlabs.logstory.config.ConfigAction
import java.util.*

/**
 * When a log line matches a ConfigPattern, this
 * data holds the pattern's action together with the
 * matched field values.
 */
data class LogLineMatch(
        val actions : EnumSet<ConfigAction>,
        val fields : Map<String, String>) {
}
