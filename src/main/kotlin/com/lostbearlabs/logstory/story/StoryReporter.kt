package com.lostbearlabs.logstory.story

import java.io.PrintStream


public class StoryReporter {

    public fun print(stories: List<Story>) {
        print(stories, System.out)
    }

    fun print(stories: List<Story>, stream: PrintStream) {
        stories.forEach {
            print(it, stream)
        }
    }

    fun print(story: Story, stream: PrintStream) {
        stream.println()
        stream.println("===============")
        if (!story.fields.isEmpty()) {
            story.fields.keys.sorted().forEach {
                stream.println("= ${it}: ${story.fields.get(it)}")
            }
            stream.println("===============")
            stream.println()
        }

        story.lines.forEach {
            stream.println(it.text)
        }

    }
}