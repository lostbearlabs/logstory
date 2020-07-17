package com.lostbearlabs.logstory

import com.lostbearlabs.logstory.config.Config
import com.lostbearlabs.logstory.story.Story
import java.io.PrintStream

class StatsReporter {

    fun print(stories: List<Story>, config: Config) {
        val stream = System.out
        print(stream, stories, config)
    }

    fun print(stream: PrintStream, stories: List<Story>, config: Config) {
        stream.println()
        stream.println("======== STATS ==========")

        val counts = HashMap<String, Int>()
        stories.forEach { story ->
            story.lines.forEach { line ->
                line.matches.forEach { match ->
                    val prev = counts[match.patternName]
                    if( prev==null ) {
                        counts[match.patternName] = 1
                    } else {
                        counts[match.patternName] = prev + 1
                    }
                }
            }
        }

        config.patterns.forEach { pattern ->
           stream.println("${counts[pattern.patternName]}   --   ${pattern.pattern}")
        }
    }
}