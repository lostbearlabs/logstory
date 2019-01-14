package com.lostbearlabs.logstory.config

import java.util.*
import java.util.regex.Pattern
import java.util.Collections
import java.lang.reflect.InvocationTargetException



/**
 * A regular expression to look for in the log file, along with any
 * actions associated with it.
 */
data class ConfigPattern(val actions : EnumSet<ConfigAction>, val pattern : Pattern) {


    /**
     * The default data-class implementation of equality doesn't work because Pattern
     * doesn't implement equality.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as ConfigPattern

        return actions==other.actions && pattern.toString()==other.pattern.toString()
    }

    val fieldNames : Set<String>
        get() = getNamedGroups(pattern)

    // see: https://stackoverflow.com/a/15596145/4540
    @Throws(NoSuchMethodException::class, SecurityException::class, IllegalAccessException::class, IllegalArgumentException::class, InvocationTargetException::class)
    private fun getNamedGroups(regex: Pattern): Set<String> {

        val namedGroupsMethod = Pattern::class.java.getDeclaredMethod("namedGroups")
        namedGroupsMethod.isAccessible = true

        var namedGroups: Map<String, Int>? = null
        namedGroups = namedGroupsMethod.invoke(regex) as Map<String, Int>

        if (namedGroups == null) {
            throw InternalError()
        }

        return Collections.unmodifiableMap(namedGroups).keys
    }

}
