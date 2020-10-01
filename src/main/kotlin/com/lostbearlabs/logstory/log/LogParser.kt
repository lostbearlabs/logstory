package com.lostbearlabs.logstory.log

import com.lostbearlabs.logstory.config.Config
import java.io.File


class LogParser {

    fun parseFile(file: File, cfg: Config): List<LogLine> {
        // TODO: this will truncate at 2GB, really should read
        // it line-by-line instead
        println("parsing log file: ${file.path}")
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
            if( n%100000==0 ) {
                println("... parse ${n}/${ar.size}")
            }
        }

        println("... ${lst.size} lines matched at least one pattern")
        return lst
    }

}