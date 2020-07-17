package com.lostbearlabs.logstory.config

/**
 * In-memory representation of a config file.
 */
data class Config(
        val patterns: ArrayList<ConfigPattern>,
        val filters: Set<ConfigFilter>,
        val directives: Set<ConfigDirective>) {

    /**
     * If ANY end line is marked as required, then
     */
    fun isEndRequired(): Boolean {
        return patterns.stream().anyMatch { it.actions.contains(ConfigAction.END) && it.actions.contains(ConfigAction.REQUIRED) }
    }
}