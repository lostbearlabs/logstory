package com.lostbearlabs.logstory.story


public class StoryReporter {

    public fun print(stories: List<Story>) {
        stories.forEach {
            print(it)
        }
    }

    fun print(story: Story) {
        System.out.println()
        System.out.println("===============")
        story.lines.forEach {
            System.out.println( it.text )
        }
    }
}