package tanvd.bayou.prototype.annotations.processing

import tanvd.bayou.prototype.annotations.kotlin.AndroidFunctions
import java.io.InputStreamReader
import kotlin.reflect.KClass

class FunctionsProcessor {
    fun process() {
        val json = InputStreamReader(FunctionsProcessor::class.java.getResourceAsStream("/android_functions.json")).use {
            it.readText()
        }
        val functionArray = JsonUtils.readValue(json, List::class as KClass<List<String>>)
        val functionNameRegex = Regex(".*\\.([^.]+)\\(.*")
        val result = functionArray.filter { it.endsWith(")") }
                .map {
                    functionNameRegex.find(it)!!.groupValues[1]
                }
                .filter { it[0].isLowerCase() }
                .distinct().sorted()

        println(result.joinToString(separator = ",\n"))
    }
}

fun main(vararg str: String) {
    FunctionsProcessor().process()
}