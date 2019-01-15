package com.lostbearlabs.logstory.log

import com.lostbearlabs.logstory.config.Config
import com.lostbearlabs.logstory.config.ConfigAction
import com.lostbearlabs.logstory.config.ConfigPattern
import org.junit.Test
import java.util.*
import java.util.regex.Pattern
import kotlin.test.assertEquals

class LogLineParserTest {

    @Test
    fun parse_simpleMatch_lineIsPopulated() {
        val text = "abcxyz"
        val parser = LogLineParser()
        val config = givenConfig()

        val line = parser.parse(0, text, config.patterns);

        val expected = LogLine(0, text,
                setOf(
                        LogLineMatch(
                                EnumSet.of(ConfigAction.MATCH),
                                mapOf(
                                        Pair("foo", "abc"),
                                        Pair("bar", "xyz")
                                )
                        )))

        assertEquals(expected, line)
    }


    fun givenConfig(): Config {
        val actions = EnumSet.of(ConfigAction.MATCH)
        val interleaved = true
        var rx = Pattern.compile("(?<foo>abc)(?<bar>xyz)")
        var pattern = ConfigPattern(actions, rx)
        return Config(setOf(pattern), interleaved, HashSet())
    }

}

