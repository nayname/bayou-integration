/*
Copyright 2017 Rice University

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package tanvd.bayou.implementation.model.android.synthesizer

import org.apache.logging.log4j.LogManager
import org.eclipse.jface.text.Document
import tanvd.bayou.implementation.model.android.synthesizer.dsl.DSubTree
import java.net.URLClassLoader
import java.util.*

class Synthesizer {
    private val logger = LogManager.getLogger("Bayou.Synthesizer")

    fun execute(parser: Parser, ast: DSubTree): List<String> {
        val synthesizedPrograms = LinkedList<String>()

        classLoader = URLClassLoader.newInstance(parser.classpathURLs)

        val cu = parser.cu
        val programs = ArrayList<String>()
        val visitor = Visitor(ast, Document(parser.source), cu)
        try {
            cu.accept(visitor)
            if (visitor.synthesizedProgram == null)
                listOf("ERROR")
            val program = visitor.synthesizedProgram!!.replace("\\s".toRegex(), "")
            if (!programs.contains(program)) {
                programs.add(program)
                synthesizedPrograms.add(visitor.synthesizedProgram!!)
            }
        } catch (e: SynthesisException) {
            print(e.message + e.stackTrace)
            logger.error("Got exception during synthesis of ast $ast", e)
        }


        return synthesizedPrograms
    }

    companion object {
        lateinit var classLoader: ClassLoader
    }
}
