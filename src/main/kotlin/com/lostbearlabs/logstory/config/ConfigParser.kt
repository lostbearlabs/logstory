package com.lostbearlabs.logstory.config

import java.io.File
import java.text.ParseException
import java.util.*
import java.util.regex.Pattern
import kotlin.collections.HashSet

// TODO: VALIDATION?
// -- check that use of END/REQUIRED is consistent?

/**
 * Parser that reads the text of a .cfg file and parses it into a Config object in memory.
 */
class ConfigParser {
    val filterLine = "filter (\\w+) (\\w+)".toRegex()
    val patternLine = "([\\w, ]+): (.+)".toRegex()

    public fun parseFile(file: File): Config {
        val text = file.readText()
        return this.parseString(text)
    }

    fun parseString(text: String): Config {
        val filters = HashSet<ConfigFilter>()
        val patterns = ArrayList<ConfigPattern>()
        val directives = HashSet<ConfigDirective>();
        val files = ArrayList<File>()

        val lines = text.split("\n", "\r")
        var lineNumber = 1
        lines.forEach {

            // trim any comments
            val line = it.split("#")[0]

            if (line == "" || parseFilter(line, filters) || parsePattern(line, patterns)
                    || parseDirective(line, directives) || parseFile(line, files)) {
                // noop
            } else {
                throw ParseException("invalid line: $line", lineNumber)
            }

            lineNumber++
        }

        return Config(patterns, filters, directives, files)
    }

    fun parseFile(line: String, files: ArrayList<File>): Boolean {
        val ar = line.split(" ", ",")
        if (ar.size == 2 && ar[0] == "!file") {
            files.add(File(ar[1]));
            return true;
        }
        return false;
    }

    fun parseDirective(line: String, directives: HashSet<ConfigDirective>): Boolean {
        val ar = line.split(" ", ",")
        var found = false;
        ar.forEach {
            ConfigDirective.values().forEach { en ->
                if ("!" + en.toString().toUpperCase() == it.toUpperCase()) {
                    directives.add(en)
                    found = true
                }
            }
        }
        return found
    }

    fun parsePattern(line: String, patterns: ArrayList<ConfigPattern>): Boolean {
        val m = this.patternLine.matchEntire(line)
        if (m == null) {
            return false
        }

        try {
            var actions = parseActions(m.groupValues.get(1))
            var regex = Pattern.compile(m.groupValues.get(2))

            val pattern = ConfigPattern(actions, regex)
            patterns.add(pattern)
        } catch (e: IllegalArgumentException) {
            return false
        }

        return true
    }

    fun parseActions(st: String): EnumSet<ConfigAction> {
        val actions = HashSet<ConfigAction>()
        val ar = st.split(" ", ",")
        ar.forEach {
            ConfigAction.values().forEach { en ->
                if (en.toString().toUpperCase() == it.toUpperCase()) {
                    actions.add(en)
                }
            }
        }
        return EnumSet.copyOf(actions)
    }

    fun parseFilter(line: String, filters: HashSet<ConfigFilter>): Boolean {
        val m = this.filterLine.matchEntire(line)
        if (m == null) {
            return false
        }

        val filter = ConfigFilter(m.groupValues.get(1), m.groupValues.get(2))
        filters.add(filter)

        return true
    }

}