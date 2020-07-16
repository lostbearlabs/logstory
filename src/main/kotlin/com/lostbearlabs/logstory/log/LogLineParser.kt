package com.lostbearlabs.logstory.log

import com.lostbearlabs.logstory.config.ConfigPattern


public class LogLineParser {

    fun parse(lineNumber: Int,
              text: String,
              patterns: ArrayList<ConfigPattern>): LogLine {

        val matches = ArrayList<LogLineMatch>()

        patterns.forEach {
            parse(text, it, matches)
        }

        return LogLine(lineNumber, text, matches)
    }

    private fun parse(text: String, pattern: ConfigPattern,
                      matches: ArrayList<LogLineMatch>) {

        val m = pattern.pattern.matcher(text)
        while (m.find()) {
            val fields = HashMap<String, String>()
            pattern.fieldNames.forEach {
                fields.put(it, m.group(it))
            }

            val match = LogLineMatch(pattern.actions, fields, pattern.patternName)
            matches.add(match)
        }

    }

}