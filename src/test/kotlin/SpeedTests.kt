import io.kotest.matchers.shouldBe
import org.example.allFileNames
import org.example.solveCoroutines
import org.example.solveRegular
import org.example.testDirPath
import org.junit.jupiter.api.Test
import java.io.File
import java.io.PrintWriter
import kotlin.system.measureTimeMillis

typealias TestFunction = (File) -> String

class SpeedTests {

    @Test
    fun regular() = doTest("regular", ::solveRegular)

    @Test
    fun coroutines() = doTest("coroutines", ::solveCoroutines)

    private fun doTest(testName: String, testFunction: TestFunction) {
        val reportFile = File("reports/$testName.csv")
        reportFile.createNewFile()
        PrintWriter(reportFile.writer()).use { csvWriter ->
            csvWriter.println("test,timeMs")

            for (testNum in allFileNames()) {
                val inputFile = testDirPath.resolve(testNum).toFile()
                val result: String
                val timeMs = measureTimeMillis {
                    result = testFunction(inputFile)
                }
                csvWriter.println("$testNum,$timeMs")

                val answerFile = testDirPath.resolve("$testNum.a").toFile()
                val expected = answerFile.readText().trim()

                result shouldBe expected
            }
        }
    }
}
