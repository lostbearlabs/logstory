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

        // End any pending stories that will be restarted by this line
        val storiesRestarted = this.getStoriesRestartedByLine(line, pendingStories)
        pendingStories.removeAll(storiesRestarted)
        completedStories.addAll(storiesRestarted)

        // Add this line to any pending stories that it matches
        // (And update them with any new fields from the line)
        applyLineToStories(line, pendingStories)

        // End any pending stories that are ended by this line
        val storiesEnded = this.getStoriesEndedByLine(line, pendingStories)
        pendingStories.removeAll(storiesEnded)
        completedStories.addAll(storiesEnded)

        // Create any new story that might be started by this line
        val storiesStarted =  this.getStoriesStartedByLine(line, filters, pendingStories)
        pendingStories.addAll(storiesStarted)
    }

    private fun getStoriesStartedByLine(line: LogLine, filters: Set<ConfigFilter>, pendingStories : ArrayList<Story>): Collection<Story> {
        val storiesAdded = ArrayList<Story>()

        // This line might indicate the start of a new story.
        var start = false
        var restart = false

        // Any new story must honor all filters.  Just apply them as values
        // to the story when it's created, and that will prevent any mis-matches
        // from the start.
        val story = Story(ArrayList())
        filters.forEach {
            story.fields[it.fieldName] = it.fieldValue
        }

        // Is there a START pattern that matches this line?  If so,
        // the line begins a story.
        line.matches.forEach {
            if (matchValuesToStory(it, story)) {
                start = start || it.actions.contains(ConfigAction.START)
                restart = restart || it.actions.contains(ConfigAction.RESTART)
            }
        }

        // Is there already a pending story that matches this START line?  If so,
        // then don't create another overlapping story
        pendingStories.forEach {story ->
            line.matches.forEach { match ->
                if (matchValuesToStory(match, story)) {
                    start = false
                }
            }
        }

        if (start || restart) {
            story.addLine(line)
            storiesAdded.add(story)
        }

        return storiesAdded
    }

    private fun getStoriesRestartedByLine(line: LogLine, pendingStories: ArrayList<Story>) : ArrayList<Story> {
        return getStoriesMatchedByLineHavingAction(line, pendingStories, ConfigAction.RESTART)
    }

    private fun getStoriesEndedByLine(line: LogLine, pendingStories: ArrayList<Story>) : ArrayList<Story> {
        return getStoriesMatchedByLineHavingAction(line, pendingStories, ConfigAction.END)
    }

    private fun getStoriesMatchedByLineHavingAction(line: LogLine, pendingStories: ArrayList<Story>, action: ConfigAction) : ArrayList<Story> {
        val storiesMatched = ArrayList<Story>()

        pendingStories.forEach {
            if( storyMatchedByLineHavingAction(line, it, action)) {
                storiesMatched.add(it)
            }
        }

        return storiesMatched
    }

    private fun storyMatchedByLineHavingAction(line: LogLine, story: Story, action: ConfigAction): Boolean {
        var matched = false
        line.matches.forEach {
            if (matchValuesToStory(it, story)) {
                matched = matched || it.actions.contains(action)
            }
        }
        return matched
    }





    private fun applyLineToStories(line: LogLine, pendingStories: ArrayList<Story>) {
        pendingStories.forEach {
            applyLineToStory(line, it)
        }
    }

    private fun applyLineToStory(line: LogLine, story: Story) {
        var matchFound = false

        line.matches.forEach {
            if (matchValuesToStory(it, story)) {
                matchFound = true
            }
        }

        if (matchFound) {
            story.addLine(line)
        }
    }

    private fun matchValuesToStory(match: LogLineMatch, story: Story): Boolean {
        match.fields.forEach {
            if (story.fields.contains(it.key) && story.fields[it.key] != it.value) {
                // there's already a conflicting value, this line is not part of this story
                return false
            }
        }

        match.fields.forEach {
            // from above, there's either no value yet or it's the same -- absorb it
            story.fields[it.key] = it.value
        }

        return true
    }

}