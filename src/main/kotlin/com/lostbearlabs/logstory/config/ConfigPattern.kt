package com.lostbearlabs.logstory.config

import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.regex.Pattern


/**
 * A regular expression to look for in the log file, along with any
 * actions associated with it.
 */
data class ConfigPattern(val actions: EnumSet<ConfigAction>, val pattern: Pattern) {
    val fieldNames: Set<String>

    init {
        this.fieldNames = getNamedGroups(pattern)
    }

    // To uniquely identify this pattern, just return its hash code, which will be the actual object ID
    val patternName: String
        get() = this.hashCode().toString()

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

    // see: https://stackoverflow.com/a/15596145/4540
    // This would be the nice  way to get group names from the regular expression, but it requires reflection access
    // which generates a warning and will eventually stop working.
    @Throws(NoSuchMethodException::class, SecurityException::class, IllegalAccessException::class, IllegalArgumentException::class,
            InvocationTargetException::class)
    private fun getNamedGroupsUsingReflection(regex: Pattern): Set<String> {

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


    // Since we can't rely on getNamedGroupsUsingReflection (above) we'll just try to extract group
    // names from the regular expression by using another regular expression to look for them!
    private fun getNamedGroups(regex: Pattern): Set<String> {
        val groups = HashSet<String>()
        // looking for things like this: "(<name>"
        val groupNamePattern = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>")
        val m = groupNamePattern.matcher(regex.pattern())
        while (m.find()) {
            groups.add(m.group(1).toString())
        }
        return groups
    }
}
