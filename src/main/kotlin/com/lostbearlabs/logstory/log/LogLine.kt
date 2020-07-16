package com.lostbearlabs.logstory.log

/**
 * A log line, together with any pattern match data.
 * Pattern matches are in the same order as the patterns themselves from the config.
 */
data class LogLine(
        val lineNumber: Int,
        val text: String,
        val matches: ArrayList<LogLineMatch>) {
}