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
        val configText = """
            start: See (?<name>\w+)\.
            match: See (?<name>\w+) run\.
            end: Run, (?<name>\w+), run\!
        """.trimIndent()

        val config = ConfigParser().parseString(configText)

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
        val configText = """
            restart: See \w+\.
            match: See (?<name>\w+) run\.
            end: Run, (?<name>\w+), run\!
        """.trimIndent()

        val config = ConfigParser().parseString(configText)

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
        val configText = """
            restart: See (?<name>\w+)\.
            match: See (?<name>\w+) \w+\.
            end: \w+, (?<name>\w+), \w+\!
        """.trimIndent()

        val config = ConfigParser().parseString(configText)

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
        val configText = """
            start: See (?<name>\w+)\.
            match: See (?<name>\w+) run\.
            end: Run, (?<name>\w+), run\!
            filter name Bob
        """.trimIndent()

        val config = ConfigParser().parseString(configText)

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

    @Test
    fun run_multipleStartsMatch_onlyReturnsOneStory() {
        val configText = """
            start: See (?<name>\w+)\.
            match: See (?<name>\w+) run\.
            match: Run, (?<name>\w+), run\!
        """.trimIndent()

        val config = ConfigParser().parseString(configText)

        val lines = givenLinesWithBobTwice(config)

        val stories = StoryExtractor().run(lines, config)

        val bobStory = """
            See Bob.
            See Bob run.
            Run, Bob, run!
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
        assertEquals(setOf(bobStory, aliceStory), extractedStories)
    }

    @Test
    fun run_oneHasRequiredMatch_onlyReturnsOneStory() {
        val configText = """
            start: See (?<name>\w+)\.
            match, required: See (?<name>\w+) run\.
            end, required: Run, (?<name>\w+), run\!
        """.trimIndent()

        val config = ConfigParser().parseString(configText)

        val logText = """
            See Bob.
            See Alice.
            See Bob run.
            Run, Bob, run!
            Run, Alice, run!
        """.trimIndent()

        var lines = LogParser().parseText(logText, config)

        val stories = StoryExtractor().run(lines, config)

        val bobStory = """
            See Bob.
            See Bob run.
            Run, Bob, run!
        """.trimIndent()

        val extractedStories = stories.stream().map { story -> story.toText() }.collect(Collectors.toSet());
        assertEquals(setOf(bobStory), extractedStories)
    }


    @Test
    fun run_oneMissingNonRequiredMatch_onlyReturnsOneStory() {
        val configText = """
            start: See (?<name>\w+)\.
            match: See (?<name>\w+) run\.
            end, required: Run, (?<name>\w+), run\!
        """.trimIndent()

        val config = ConfigParser().parseString(configText)

        val logText = """
            See Bob.
            See Alice.
            See Bob run.
            Run, Bob, run!
            Run, Alice, run!
        """.trimIndent()

        var lines = LogParser().parseText(logText, config)

        val stories = StoryExtractor().run(lines, config)

        val bobStory = """
            See Bob.
            See Bob run.
            Run, Bob, run!
        """.trimIndent()

        val aliceStory = """
            See Alice.
            Run, Alice, run!
        """.trimIndent()

        val extractedStories = stories.stream().map { story -> story.toText() }.collect(Collectors.toSet());
        assertEquals(setOf(bobStory, aliceStory), extractedStories)
    }

    @Test
    fun run_oneWithForbiddenMatch_onlyReturnsOneStory() {
        val configText = """
            start: See (?<name>\w+)\.
            match, forbidden: See (?<name>\w+) run\.
            end, required: Run, (?<name>\w+), run\!
        """.trimIndent()

        val config = ConfigParser().parseString(configText)

        val logText = """
            See Bob.
            See Alice.
            See Bob run.
            Run, Bob, run!
            Run, Alice, run!
        """.trimIndent()

        var lines = LogParser().parseText(logText, config)

        val stories = StoryExtractor().run(lines, config)

        val aliceStory = """
            See Alice.
            Run, Alice, run!
        """.trimIndent()

        val extractedStories = stories.stream().map { story -> story.toText() }.collect(Collectors.toSet());
        assertEquals(setOf(aliceStory), extractedStories)
    }

    @Test
    fun run_valuesOverlap_honorConfigOrderAndStartLine() {
        val configText = """
            start: x=(?<x>\d+) y=(?<y>\d+)
            match: x=(?<x>\d+)
            match: y=(?<y>\d+)
            match: y=(?<x>\d+)
        """.trimIndent()

        val config = ConfigParser().parseString(configText)

        val logText = """
            A y=1
            B x=2 y=3
            C x=4
            D y=5
            E y=3
            F x=6
            G x=2
            H y=2
        """.trimIndent()

        var lines = LogParser().parseText(logText, config)

        val stories = StoryExtractor().run(lines, config)

        val story = """
            B x=2 y=3
            E y=3
            G x=2
            H y=2
        """.trimIndent()

        val extractedStories = stories.stream().map(Story::toText).collect(Collectors.toSet());
        assertEquals(setOf(story), extractedStories)
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

    fun givenLinesWithBobTwice(config: Config): List<LogLine> {
        val logText = """
            See Bob.
            See Alice.
            See Bob run.
            See Alice run.
            Run, Bob, run!
            Run, Alice, run!
            See Bob.
            See Bob run.
            Run, Bob, run!
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

}