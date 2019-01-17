package com.lostbearlabs.logstory.config

import java.util.*
import java.util.regex.Pattern


/**
 * A regular expression to look for in the log file, along with any
 * actions associated with it.
 */
data class ConfigPattern(val actions: EnumSet<ConfigAction>, val pattern: Pattern) {


    /**
     * The default data-class implementation of equality doesn't work because Pattern
     * doesn't implement equality.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ConfigPattern

        return actions == other.actions && pattern.toString() == other.pattern.toString()
    }

    val fieldNames: Set<String>
        get() = getNamedGroups(pattern)

    /**
    @Throws(NoSuchMethodException::class, SecurityException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    private fun getNamedGroups(regex: Pattern): Set<String> {

    val namedGroupsMethod = Pattern::class.java.getDeclaredMethod("namedGroups")
    namedGroupsMethod.isAccessible = true

    var namedGroups = namedGroupsMethod.invoke(regex)

    if (namedGroups == null) {
    throw InternalError()
    }

    @Suppress("UNCHECKED_CAST")
    namedGroups as Map<String, Int>
    return Collections.unmodifiableMap(namedGroups).keys
    }
     **/


    // see: https://stackoverflow.com/a/15596145/4540
    // The nice way to do this requires reflection access which generates a warning and will eventually stop working.
    // So we're just going to try to parse the regex itself, which is also a dicey proposition.
    private fun getNamedGroups(regex: Pattern): Set<String> {
        val groups = HashSet<String>()
        val groupNamePattern = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>")
        val m = groupNamePattern.matcher(regex.pattern())
        while( m.find()) {
            groups.add(m.group(1).toString())
        }
        return groups
    }
}
