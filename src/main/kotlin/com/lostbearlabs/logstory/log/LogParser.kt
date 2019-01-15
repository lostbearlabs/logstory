package com.lostbearlabs.logstory.log

import com.lostbearlabs.logstory.config.Config
import java.io.File


class LogParser {

    fun parseFile(file: File, cfg: Config): List<LogLine> {
        // TODO: this will truncate at 2GB, really should read
        // it line-by-line instead
        val text = file.readText()
        return this.parseText(text, cfg)
    }

    fun parseText(text: String, cfg: Config): List<LogLine> {
        val ar = text.split("\r", "\n")
        var lst = ArrayList<LogLine>()

        var n = 1
        ar.forEach {
            var line = LogLineParser().parse(n, it, cfg.patterns)
            if (!line.matches.isEmpty()) {
                lst.add(line)
            }
            n++
        }

        return lst
    }

}