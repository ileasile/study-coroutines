package org.example

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.BufferedReader
import java.io.File
import java.io.PrintWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.name
import kotlin.io.path.reader
import kotlin.io.path.writeText
import kotlin.io.path.writer

interface Unpacker {
    fun currentLength (): Int
    fun consumeChar(char: Char): Boolean
    fun getChar(index: Int): Char
}

class InitialUnpacker(
    private val builder: StringBuilder
): Unpacker {
    override fun currentLength() = builder.length

    override fun consumeChar(char: Char): Boolean {
        builder.append(char)
        return true
    }

    override fun getChar(index: Int) = builder[index]
}

class CommonPrefixUnpacker(
    private val builder: StringBuilder
): Unpacker {
    var prefixLength: Int = builder.length
    private var _currentLength = 0

    override fun currentLength() = _currentLength

    override fun consumeChar(char: Char): Boolean {
        return if (_currentLength == prefixLength) false
        else if (builder[_currentLength] == char) { ++_currentLength; true }
        else { false }
    }

    override fun getChar(index: Int): Char {
        return builder[index]
    }

    fun clear() {
        prefixLength = _currentLength
        _currentLength = 0
    }
}

fun main() {
    //println(solveRegular(testDirPath.resolve("10").toFile()))
    genTest("34", "41")
}

val testDirPath: Path = Paths.get("tests")

fun genTest(inputTest: String, newTest: String) {
    val inputTestReader = testDirPath.resolve(inputTest).reader()
    val inputTestLines = inputTestReader.readLines()

    val newTestWriter = PrintWriter(testDirPath.resolve(newTest).writer())
    val newTestLinesCount = 1000_000

    newTestWriter.println(newTestLinesCount)
    for (i in 0 ..< newTestLinesCount) {
        newTestWriter.println(inputTestLines[1])
    }
    newTestWriter.close()

    val result = solveRegular(testDirPath.resolve(newTest).toFile())
    testDirPath.resolve("$newTest.a").writeText(result)
}

fun allFileNames(): List<String> = sequence {
    for (file in Files.walk(testDirPath)) {
        if (Files.isRegularFile(file) && !file.name.endsWith(".a")) {
            yield(file.name)
        }
    }
}.toList().sorted()

fun solveRegular(inputFile: File): String {
    val bufferedReader = inputFile.bufferedReader()

    return bufferedReader.use { reader ->
        val stringsCount = reader.readInt()
        val maxCommonPrefix = StringBuilder()
        Algo(reader.readLine(), InitialUnpacker(maxCommonPrefix)).unpack()

        val unpacker = CommonPrefixUnpacker(maxCommonPrefix)
        repeat(stringsCount - 1) {
            Algo(reader.readLine(), unpacker).unpack()
            unpacker.clear()
        }

        maxCommonPrefix.setLength(unpacker.prefixLength)
        maxCommonPrefix.toString()
    }
}

fun solveCoroutines(inputFile: File): String {
    val bufferedReader = inputFile.bufferedReader()

    return bufferedReader.use { reader ->
        val stringsCount = reader.readInt()
        val maxCommonPrefix = StringBuilder()

        Algo(reader.readLine(), InitialUnpacker(maxCommonPrefix)).unpack()
        val unpacker = CommonPrefixUnpacker(maxCommonPrefix)

        runBlocking(Dispatchers.Default) {
            var line = async { reader.readLine() }
            val lastI = stringsCount - 2

            repeat(lastI + 1) { i ->
                val line2 = line
                if (i != lastI) {
                    line = async { reader.readLine() }
                }
                Algo(line2.await(), unpacker).unpack()
                unpacker.clear()
            }
        }

        maxCommonPrefix.setLength(unpacker.prefixLength)
        maxCommonPrefix.toString()
    }
}

class Algo(private val symbols: String, private val unpacker: Unpacker) {
    private var i = 0

    fun unpack() {
        while (i < symbols.length && symbols[i] != ']') {
            val multiplier = symbols[i].digitToIntOrNull()
            if (multiplier != null) {
                i += 2
                val prevLength = unpacker.currentLength()
                unpack()
                if (i == -1) return
                val newLength = unpacker.currentLength()

                repeat(multiplier - 1) {
                    for (d in prevLength ..< newLength) {
                        if(!unpacker.consumeChar(unpacker.getChar(d))) {
                            i = -1
                            return
                        }
                    }
                }
            } else {
                if(!unpacker.consumeChar(symbols[i])) {
                    i = -1
                    return
                }
                ++i
            }
        }
        ++i
    }
}


fun BufferedReader.readInt(): Int = readLine().toInt()