package com.lostbearlabs.logstory

import com.lostbearlabs.logstory.config.ConfigParser
import com.lostbearlabs.logstory.log.LogParser
import com.lostbearlabs.logstory.story.StoryExtractor
import com.lostbearlabs.logstory.story.StoryReporter
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class ExampleVerificationTest {

    @Test
    fun verifyAllExamples() {
        var names = HashSet<String>()
        var dir = "./examples"
        File(dir).walk().forEach {
            if( it.isFile ) {
                names.add(it.nameWithoutExtension)
            }
        }

        names.toList().sorted().forEach {
            verifyExample(dir, it)
        }
    }

    fun verifyExample(dir : String, name: String) {
        var logFile = File(dir, name + ".log")
        var cfgFile = File(dir, name + ".cfg")
        var outFile = File(dir, name + ".out")

        assertTrue(logFile.exists(), "File not found: $logFile")
        assertTrue(cfgFile.exists(), "File not found: $cfgFile")
        assertTrue(outFile.exists(), "File not found: $outFile")

        val expectedText = outFile.readText(StandardCharsets.UTF_8)

        var tmpFile = File.createTempFile("logstory", "out")
        tmpFile.deleteOnExit()

        val config = ConfigParser().parseFile(cfgFile)
        val lines = LogParser().parseFile(logFile, config)
        val stories = StoryExtractor().run(lines, config)


        val baos = ByteArrayOutputStream()
        PrintStream(baos, true, "UTF-8").use {
            StoryReporter().printAllStories(stories, it)
        }
        val generatedText = String(baos.toByteArray(), StandardCharsets.UTF_8)

        assertEquals(expectedText, generatedText, "Output does not match for file: $outFile")
        System.out.println("verified example $name ok")
    }
}