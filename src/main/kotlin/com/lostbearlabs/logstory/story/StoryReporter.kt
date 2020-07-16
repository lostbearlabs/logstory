package com.lostbearlabs.logstory.story

import java.io.PrintStream


public class StoryReporter {

    public fun print(stories: List<Story>) {
        val stream = System.out

        if( stories.size <= 10 ) {
            printAllStories(stories, stream)
        } else {
            val sorted = stories.sortedBy { story -> story.lines.size }
            printStory(sorted[0], stream)
            printStory(sorted.last(), stream)
            printSummary(sorted, stream)
        }
    }

    fun printSummary(sortedStories: List<Story>, stream: PrintStream) {
        stream.println()
        stream.println("===============")
        stream.println("total num stories: ${sortedStories.size}")
        stream.println("shortest length: ${sortedStories[0].lines.size}")
        stream.println("longest length: ${sortedStories.last().lines.size}")
    }

    fun printAllStories(stories: List<Story>, stream: PrintStream) {
        for (story in stories) {
            printStory(story, stream)
        }
    }

    fun printStory(story: Story, stream: PrintStream) {
        stream.println()
        stream.println("===============")
        if (!story.fields.isEmpty()) {
            story.fields.keys.sorted().forEach {
                stream.println("= ${it}: ${story.fields.get(it)}")
            }
            stream.println("===============")
            stream.println()
        }

        var n = 0
        for (line in story.lines) {
            stream.println(line.text)
            n++
            if (n > 100) {
                stream.println("... truncated, ${story.lines.size} lines total")
                break
            }
        }
    }

}
