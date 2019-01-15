package com.lostbearlabs.logstory.config

/**
 * In-memory representation of a config file.
 */
data class Config(
        val patterns: Set<ConfigPattern>,
        val interleaved: Boolean,
        val filters : Set<ConfigFilter>) {
}