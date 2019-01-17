package com.lostbearlabs.logstory.story

import com.lostbearlabs.logstory.config.Config
import com.lostbearlabs.logstory.config.ConfigParser
import com.lostbearlabs.logstory.log.LogLine
import com.lostbearlabs.logstory.log.LogParser
import org.junit.Test
import java.util.stream.Collectors
import java.util.stream.Collectors.toSet
import kotlin.test.assertEquals


class StoryExtractorTest {

    @Test
    fun run_happyPath_getsStory() {
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

        val extractedStories = stories.stream().map{ story -> story.toText()}.collect(Collectors.toSet());
        assertEquals(setOf(aliceStory, bobStory), extractedStories)
    }

    fun givenLines(config: Config) : List<LogLine> {
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
}