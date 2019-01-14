package com.lostbearlabs.logstory.config


/**
 * A filter on which field values from the log file to find interesting.
 */
data class ConfigFilter(val fieldName: String, val fieldValue: String) {
}