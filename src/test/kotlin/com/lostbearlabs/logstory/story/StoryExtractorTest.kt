package com.lostbearlabs.logstory.story

import com.lostbearlabs.logstory.config.Config
import com.lostbearlabs.logstory.config.ConfigParser
import com.lostbearlabs.logstory.log.LogLine
import com.lostbearlabs.logstory.log.LogParser
import org.junit.Test
import java.util.stream.Collectors
import kotlin.test.assertEquals


class StoryExtractorTest {

    @Test
    fun run_happyPath_getsStories() {
        val config = givenConfig()
        val lines = givenLines(config)

        val stories = StoryExtractor().run(lines, config)

        val bobStory = """
            See Bob.
            See Bob run.
            Run, Bob, run!
        """.trimIndent()

        val aliceStory = """
            See Alice.
            See Alice run.
            Run, Alice, run!
        """.trimIndent()

        val extractedStories = stories.stream().map { story -> story.toText() }.collect(Collectors.toSet());
        assertEquals(setOf(aliceStory, bobStory), extractedStories)
    }

    @Test
    fun run_withUnparameterizedRestart_getsStories() {
        val config = givenConfigWithUnparemeterizedRestart()
        val lines = givenLines(config)

        val stories = StoryExtractor().run(lines, config)

        val bobStory = """
            See Bob.
        """.trimIndent()

        val aliceStory = """
            See Alice.
            See Bob run.
            Run, Bob, run!
        """.trimIndent()

        val extractedStories = stories.stream().map { story -> story.toText() }.collect(Collectors.toSet());
        assertEquals(setOf(aliceStory, bobStory), extractedStories)
    }

    @Test
    fun run_withParameterizedRestart_honorsParameters() {
        val config = givenConfigWithParameterizedRestart()
        val lines = givenLinesWithThreeStories(config)

        val stories = StoryExtractor().run(lines, config)

        val bobStory = """
            See Bob.
            See Bob run.
            Run, Bob, run!
        """.trimIndent()

        val bobStory2 = """
            See Bob.
            See Bob walk.
            Walk, Bob, walk!
        """.trimIndent()

        val aliceStory = """
            See Alice.
            See Alice run.
            Run, Alice, run!
        """.trimIndent()

        val extractedStories = stories.stream().map { story -> story.toText() }.collect(Collectors.toSet());
        assertEquals(setOf(aliceStory, bobStory, bobStory2), extractedStories)
    }

    @Test
    fun run_twoFieldsMatch_bothInStory() {
        val config = givenConfigWithTwoMatches();
        val lines = givenLineWithTwoMatches(config)

        val stories = StoryExtractor().run(lines, config)
        val story = stories.get(0)

        assertEquals(mapOf(Pair("a", "x"), Pair("b", "y")), story.fields)
    }

    @Test
    fun run_twoFieldsInStartLineMatch_bothInStory() {
        val config = givenConfigWithTwoMatchesInStartLine();
        val lines = givenLineWithTwoMatches(config)

        val stories = StoryExtractor().run(lines, config)
        val story = stories.get(0)

        assertEquals(mapOf(Pair("a", "x"), Pair("b", "y")), story.fields)
    }

    @Test
    fun run_withFilters_returnsMatchingStory() {
        val config = givenConfigWithBobFilter()
        val lines = givenLines(config)

        val stories = StoryExtractor().run(lines, config)

        val bobStory = """
            See Bob.
            See Bob run.
            Run, Bob, run!
        """.trimIndent()

        val extractedStories = stories.stream().map { story -> story.toText() }.collect(Collectors.toSet());
        assertEquals(setOf(bobStory), extractedStories)
    }

    fun givenConfigWithTwoMatches(): Config {

        val configText = """
            start: start
            match: a=(?<a>\w+)
            match: b=(?<b>\w+)
        """.trimIndent()

        return ConfigParser().parseString(configText)
    }

    fun givenConfigWithTwoMatchesInStartLine(): Config {

        val configText = """
            start: a=(?<a>\w+)
            match: b=(?<b>\w+)
        """.trimIndent()

        return ConfigParser().parseString(configText)
    }

    fun givenLineWithTwoMatches(config: Config): List<LogLine> {
        val logText = """
            start
            a=x b=y
        """.trimIndent()

        return LogParser().parseText(logText, config)
    }

    fun givenLines(config: Config): List<LogLine> {
        val logText = """
            See Bob.
            See Alice.
            See Bob run.
            See Alice run.
            Run, Bob, run!
            Run, Alice, run!
        """.trimIndent()

        return LogParser().parseText(logText, config)
    }

    fun givenLinesWithThreeStories(config: Config): List<LogLine> {
        val logText = """
            See Bob.
            See Alice.
            See Bob run.
            See Alice run.
            Run, Bob, run!
            See Bob.
            See Bob walk.
            Run, Alice, run!
            Walk, Bob, walk!
        """.trimIndent()

        return LogParser().parseText(logText, config)
    }

    fun givenConfig(): Config {
        val configText = """
            start: See (?<name>\w+)\.
            match: See (?<name>\w+) run\.
            end: Run, (?<name>\w+), run\!
        """.trimIndent()

        return ConfigParser().parseString(configText)
    }

    fun givenConfigWithBobFilter(): Config {
        val configText = """
            start: See (?<name>\w+)\.
            match: See (?<name>\w+) run\.
            end: Run, (?<name>\w+), run\!
            filter name Bob
        """.trimIndent()

        return ConfigParser().parseString(configText)
    }

    fun givenConfigWithUnparemeterizedRestart(): Config {
        val configText = """
            restart: See \w+\.
            match: See (?<name>\w+) run\.
            end: Run, (?<name>\w+), run\!
        """.trimIndent()

        return ConfigParser().parseString(configText)
    }

    fun givenConfigWithParameterizedRestart(): Config {
        val configText = """
            restart: See (?<name>\w+)\.
            match: See (?<name>\w+) \w+\.
            end: \w+, (?<name>\w+), \w+\!
        """.trimIndent()

        return ConfigParser().parseString(configText)
    }

}