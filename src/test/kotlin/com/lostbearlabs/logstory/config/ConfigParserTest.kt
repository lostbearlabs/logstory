package com.lostbearlabs.logstory.config

import org.junit.Test
import java.text.ParseException
import java.util.*
import java.util.regex.Pattern
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ConfigParserTest {

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
        assertEquals(listOf(expected), config.patterns);
    }

    @Test
    fun parse_forbiddenMatch_recordsMatch() {
        val rx = "abc"
        val text = "match, forbidden: $rx"
        val parser = ConfigParser()

        val config = parser.parseString(text)

        val expected = ConfigPattern(EnumSet.of(ConfigAction.MATCH, ConfigAction.FORBIDDEN), Pattern.compile(rx))
        assertEquals(listOf(expected), config.patterns);
    }

    @Test
    fun parse_multipleActions_recordsActions() {
        val rx = "abc"
        val text = "end, required: $rx"
        val parser = ConfigParser()

        val config = parser.parseString(text)

        val expected = ConfigPattern(EnumSet.of(ConfigAction.REQUIRED, ConfigAction.END), Pattern.compile(rx))
        assertEquals(listOf(expected), config.patterns)
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

    @Test
    fun parse_statsDirective_setsStats() {
        val text = """
            !stats
        """.trimIndent()
        val parser = ConfigParser()
        val config = parser.parseString(text)

        assertTrue(config.directives.contains(ConfigDirective.STATS));
    }
}

