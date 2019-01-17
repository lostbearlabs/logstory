package com.lostbearlabs.logstory.config

import org.junit.Test
import java.text.ParseException
import java.util.*
import java.util.regex.Pattern
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals


class ConfigParserTest {

    @Test
    fun parse_interleavedNotSpecified_interleavedIsFalse() {
        val text = ""
        val parser = ConfigParser()

        val config = parser.parseString(text)

        assertFalse(config.interleaved)
    }

    @Test
    fun parse_interleavedSpecifiedFalse_interleavedIsFalse() {
        val text = "interleaved: false"
        val parser = ConfigParser()

        val config = parser.parseString(text)

        assertFalse(config.interleaved)
    }

    @Test
    fun parse_interleavedSpecifiedTrue_interleavedIsTrue() {
        val text = "interleaved: true"
        val parser = ConfigParser()

        val config = parser.parseString(text)

        assertTrue(config.interleaved)
    }

    @Test
    fun parse_interleavedSpecifiedTwice_throws() {
        val text = """
            interleaved: false
            interleaved: true
        """.trimIndent()
        val parser = ConfigParser()

        assertFailsWith(ParseException::class) {
            parser.parseString(text)
        }

    }

    @Test
    fun parse_filterSpecified_recordsFilter() {
        val text = "filter x y"
        val parser = ConfigParser()

        val config = parser.parseString(text)

        val expected = ConfigFilter("x", "y")
        assertEquals(setOf(expected), config.filters);
    }

    @Test
    fun parse_unrecognizedLine_throws() {
        val text = "potato potahto"
        val parser = ConfigParser()

        assertFailsWith(ParseException::class) {
            parser.parseString(text)
        }
    }

    @Test
    fun parse_simpleMatch_recordsMatch() {
        val rx = "abc"
        val text = "match: $rx"
        val parser = ConfigParser()

        val config = parser.parseString(text)

        val expected = ConfigPattern(EnumSet.of(ConfigAction.MATCH), Pattern.compile(rx))
        assertEquals(setOf(expected), config.patterns);
    }

    @Test
    fun parse_multipleActions_recordsActions() {
        val rx = "abc"
        val text = "end, required: $rx"
        val parser = ConfigParser()

        val config = parser.parseString(text)

        val expected = ConfigPattern(EnumSet.of(ConfigAction.REQUIRED, ConfigAction.END), Pattern.compile(rx))
        assertEquals(setOf(expected), config.patterns);
    }

    @Test
    fun parse_badAction_throws() {
        val rx = "abc"
        val text = "fnord: $rx"
        val parser = ConfigParser()

        assertFailsWith(ParseException::class) {
            parser.parseString(text)
        }
    }

    @Test
    fun parse_badRegex_throws() {
        val rx = "[abc)"
        val text = "fnord: $rx"
        val parser = ConfigParser()

        assertFailsWith(ParseException::class) {
            parser.parseString(text)
        }
    }

    @Test
    fun parse_regexWithFieldNames_getsFieldNames() {
        val rx = "(?<foo>abc)(?<bar>def)"
        val text = "match: $rx"
        val parser = ConfigParser()

        val config = parser.parseString(text)

        val pattern = config.patterns.stream().findFirst().get()
        assertEquals(setOf("foo", "bar"), pattern.fieldNames);
    }

    @Test
    fun parse_endRequiredOnce_endIsRequired() {
        val text = """
            end: xyz
            end, required: abc
        """.trimIndent()
        val parser = ConfigParser()

        val config = parser.parseString(text)

        assertTrue(config.isEndRequired())
    }

    @Test
    fun parse_endRequiredNever_endIsNotRequired() {
        val text = """
            end: xyz
            end: abc
        """.trimIndent()
        val parser = ConfigParser()

        val config = parser.parseString(text)

        assertFalse(config.isEndRequired())
    }
}

