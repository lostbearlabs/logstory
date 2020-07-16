package com.lostbearlabs.logstory.story

import com.lostbearlabs.logstory.config.Config
import com.lostbearlabs.logstory.config.ConfigAction
import com.lostbearlabs.logstory.config.ConfigFilter
import com.lostbearlabs.logstory.config.ConfigPattern
import com.lostbearlabs.logstory.log.LogLine
import com.lostbearlabs.logstory.log.LogLineMatch

class StoryExtractor {

    fun run(lines: List<LogLine>, config: Config): List<Story> {
        val pendingStories = ArrayList<Story>()
        val completedStories = ArrayList<Story>()

        var n = 0
        lines.forEach {
            processLine(it, pendingStories, completedStories, config)
            n++
            if (n % 1000 == 0) {
                println("analyze: ${n}/${lines.size}")
            }
        }

        if (!config.isEndRequired()) {
            val pendingStoriesHavingRequiredMatches = this.getStoriesWithRequiredMatches(pendingStories, config.patterns)
            completedStories.addAll(pendingStoriesHavingRequiredMatches)
        }
        return completedStories
    }

    private fun processLine(line: LogLine, pendingStories: ArrayList<Story>, completedStories: ArrayList<Story>,
                            config: Config) {

        // End any pending stories that will be restarted by this line
        val storiesRestarted = this.getStoriesRestartedByLine(line, pendingStories)
        pendingStories.removeAll(storiesRestarted)
        val storiesRestartedAndHavingRequiredMatches = this.getStoriesWithRequiredMatches(storiesRestarted, config.patterns)
        completedStories.addAll(storiesRestartedAndHavingRequiredMatches)

        // Add this line to any pending stories that it matches
        // (And update them with any new fields from the line)
        applyLineToStories(line, pendingStories)

        // Accumulate any pending stories that are ended by this line
        val storiesEnded = this.getStoriesEndedByLine(line, pendingStories)
        pendingStories.removeAll(storiesEnded)
        val storiedEndedAndHavingRequiredMatches = this.getStoriesWithRequiredMatches(storiesEnded, config.patterns)
        completedStories.addAll(storiedEndedAndHavingRequiredMatches)

        // Create any new story that might be started by this line
        val storiesStarted = this.getStoriesStartedByLine(line, config.filters, pendingStories)
        pendingStories.addAll(storiesStarted)
    }

    private fun getStoriesWithRequiredMatches(stories: ArrayList<Story>, patterns: List<ConfigPattern>): List<Story> {
        return stories.filter { story -> this.storyMatchesRequiredPatterns(story, patterns) }
    }

    /**
     * If the config has required patterns, then check whether each of those patterns is present
     * in the matches for this story.
     */
    private fun storyMatchesRequiredPatterns(story: Story, patterns: List<ConfigPattern>): Boolean {
        var requiredPatternsMatched = true;
        for (pattern in patterns) {
            val required = pattern.actions.contains(ConfigAction.REQUIRED)
            val forbidden = pattern.actions.contains(ConfigAction.FORBIDDEN)
            if (required || forbidden) {
                var patternMatched = false
                for (line in story.lines) {
                    for (match in line.matches) {
                        if (match.patternName == pattern.patternName) {
                            patternMatched = true
                        }
                    }
                }
                requiredPatternsMatched = if( required ) {
                    requiredPatternsMatched && patternMatched
                } else {
                    requiredPatternsMatched && !patternMatched
                }
            }
        }

        return requiredPatternsMatched
    }

    private fun getStoriesStartedByLine(line: LogLine, filters: Set<ConfigFilter>, pendingStories: ArrayList<Story>): Collection<Story> {
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

        // Is there a START or RESTART pattern that matches this line?  If so,
        // the line begins a story.
        line.matches.forEach {
            if (matchValuesToStory(it, story)) {
                start = start || it.actions.contains(ConfigAction.START)
                restart = restart || it.actions.contains(ConfigAction.RESTART)
            }
        }

        // Is there already a pending story that matches this START line?  If so,
        // then don't create another overlapping story.
        pendingStories.forEach { pendingStory ->
            line.matches.forEach { match ->
                if (matchValuesToStory(match, pendingStory)) {
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

    private fun getStoriesRestartedByLine(line: LogLine, pendingStories: ArrayList<Story>): ArrayList<Story> {
        return getStoriesMatchedByLineHavingAction(line, pendingStories, ConfigAction.RESTART)
    }

    private fun getStoriesEndedByLine(line: LogLine, pendingStories: ArrayList<Story>): ArrayList<Story> {
        return getStoriesMatchedByLineHavingAction(line, pendingStories, ConfigAction.END)
    }

    private fun getStoriesMatchedByLineHavingAction(line: LogLine, pendingStories: ArrayList<Story>, action: ConfigAction): ArrayList<Story> {
        val storiesMatched = ArrayList<Story>()

        pendingStories.forEach {
            if (storyMatchedByLineHavingAction(line, it, action)) {
                storiesMatched.add(it)
            }
        }

        return storiesMatched
    }

    // Returns true if the current LogLine is compatible with the story values and also
    // matches some rule that has the desired action.
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