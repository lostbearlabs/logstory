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
    fun run_withRestart_getsStories() {
        val config = givenConfigWithRestart()
        val lines = givenLines(config)

        val stories = StoryExtractor().run(lines, config)

        val bobStory = """
            See Bob.
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

    fun givenConfig(): Config {
        val configText = """
            start: See (?<name>\w+)\.
            match: See (?<name>\w+) run\.
            end: Run, (?<name>\w+), run\!
        """.trimIndent()

        return ConfigParser().parseString(configText)
    }

    fun givenConfigWithRestart(): Config {
        val configText = """
            restart: See (?<name>\w+)\.
            match: See (?<name>\w+) run\.
            end: Run, (?<name>\w+), run\!
        """.trimIndent()

        return ConfigParser().parseString(configText)
    }
}