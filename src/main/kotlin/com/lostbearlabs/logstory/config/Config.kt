package com.lostbearlabs.logstory.config

import java.io.File

/**
 * In-memory representation of a config file.
 */
data class Config(
        val patterns: ArrayList<ConfigPattern>,
        val filters: Set<ConfigFilter>,
        val directives: Set<ConfigDirective>,
        val files: ArrayList<File>) {

    /**
     * If ANY end line is marked as required, then
     */
    fun isEndRequired(): Boolean {
        return patterns.stream().anyMatch { it.actions.contains(ConfigAction.END) && it.actions.contains(ConfigAction.REQUIRED) }
    }

    fun addFile(file : File) {
        this.files.add(file)
    }

}