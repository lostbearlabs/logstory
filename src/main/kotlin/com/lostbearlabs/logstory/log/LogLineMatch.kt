package com.lostbearlabs.logstory.log

import com.lostbearlabs.logstory.config.ConfigAction
import java.util.*

/**
 * When a log line matches a ConfigPattern, this
 * data holds the pattern's action together with the
 * matched field values.  We also store the pattern name,
 * to give us an easy way to check that required patterns were matched.
 */
data class LogLineMatch(
        val actions: EnumSet<ConfigAction>,
        val fields: Map<String, String>,
        val patternName: String) {
}
