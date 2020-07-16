package com.lostbearlabs.logstory.log

/**
 * A log line, together with any pattern match data.
 */
data class LogLine(
        val lineNumber: Int,
        val text: String,
        val matches: Set<LogLineMatch>) {
}