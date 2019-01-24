package com.lostbearlabs.logstory.story

import com.lostbearlabs.logstory.config.Config
import com.lostbearlabs.logstory.config.ConfigAction
import com.lostbearlabs.logstory.config.ConfigFilter
import com.lostbearlabs.logstory.log.LogLine
import com.lostbearlabs.logstory.log.LogLineMatch

// TODO: support filters
// TODO: support multiple passes

class StoryExtractor {

    fun run(lines: List<LogLine>, config: Config): List<Story> {
        val pendingStories = ArrayList<Story>()
        val completedStories = ArrayList<Story>()

        lines.forEach {
            processLine(it, pendingStories, completedStories, config.filters)
        }

        if (!config.isEndRequired()) {
            completedStories.addAll(pendingStories)
        }
        return completedStories
    }

    private fun processLine(line: LogLine, pendingStories: ArrayList<Story>, completedStories: ArrayList<Story>,
                            filters : Set<ConfigFilter>) {
        val toRemove = ArrayList<Story>()
        pendingStories.forEach {
            val completed = applyLineToStory(line, it)
            if (completed) {
                completedStories.add(it)
                toRemove.add(it)
            }
        }

        // This line *might* indicate the start of a new story.
        var start = false
        var restart = false
        val story = Story(ArrayList<LogLine>())
        filters.forEach {
            story.fields.put(it.fieldName, it.fieldValue)
        }
        line.matches.forEach {
            if (matchValuesToStory(it, story)) {
                start = start || it.actions.contains(ConfigAction.START)
                restart = restart || it.actions.contains(ConfigAction.RESTART)
            }
        }

        if (start || restart) {
            story.addLine(line)

            if (restart) {
                completedStories.addAll(pendingStories)
                toRemove.addAll(pendingStories)
            }

            pendingStories.add(story)
        }

        pendingStories.removeAll(toRemove)
    }

    /**
     * @return whether the line both matched and is an END line
     */
    private fun applyLineToStory(line: LogLine, story: Story): Boolean {
        var matchFound = false
        var endFound = false

        line.matches.forEach {
            if (matchValuesToStory(it, story)) {
                matchFound = true
                endFound = it.actions.contains(ConfigAction.END)
            }
        }

        if (matchFound) {
            story.addLine(line)
        }

        return endFound
    }

    private fun matchValuesToStory(match: LogLineMatch, story: Story): Boolean {
        match.fields.forEach {
            if (story.fields.contains(it.key) && story.fields.get(it.key) != it.value) {
                // there's already a conflicting value, this line is not part of this story
                return false
            }
        }

        match.fields.forEach {
            // from above, there's either no value yet or it's the same -- absorb it
            story.fields.put(it.key, it.value)
        }

        return true
    }

}