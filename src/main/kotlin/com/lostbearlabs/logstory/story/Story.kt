package com.lostbearlabs.logstory.story

import com.lostbearlabs.logstory.log.LogLine
import java.util.stream.Collectors


class Story(val lines: ArrayList<LogLine>) {
    val fields = HashMap<String, String>()

    fun addLine(line: LogLine) {
        this.lines.add(line)
    }

    fun toText(): String {
        return this.lines.stream().map { line -> line.text }.collect(Collectors.joining("\n"))
    }

    override fun toString(): String {
        return this.toText()
    }
}