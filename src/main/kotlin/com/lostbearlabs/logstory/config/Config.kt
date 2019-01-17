package com.lostbearlabs.logstory.config

/**
 * In-memory representation of a config file.
 */
data class Config(
        val patterns: Set<ConfigPattern>,
        val filters : Set<ConfigFilter>) {

    /**
     * If ANY end line is marked as required, then
     */
    fun isEndRequired() : Boolean {
        return patterns.stream().anyMatch { it.actions.contains(ConfigAction.END) && it.actions.contains(ConfigAction.REQUIRED) }
    }
}