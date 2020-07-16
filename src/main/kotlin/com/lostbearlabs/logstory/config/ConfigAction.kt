package com.lostbearlabs.logstory.config

/**
 * Represents the action specifiers that can be applied to a ConfigPattern.
 */
enum class ConfigAction {
    START,
    END,
    REQUIRED,
    FORBIDDEN,
    MATCH,
    RESTART
}