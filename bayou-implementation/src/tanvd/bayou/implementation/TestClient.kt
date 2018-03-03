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
package tanvd.bayou.implementation


import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import tanvd.bayou.implementation.model.android.AndroidSynthesizingModel
import tanvd.bayou.implementation.model.stdlib.StdlibSynthesizingModel
import java.nio.file.Files
import java.nio.file.Paths

internal object TestClient {
    private val _testDialog = """
import edu.rice.cs.caper.bayou.annotations.Evidence;
import java.util.List;

public class TestUtil {

    void add(List<String> items, String item) {
        Evidence.apicalls("add");
    }
}
        """

    private val NUM_SAMPLES = "num_samples"

    private val NUM_PROGRAMS = "num_programs"

    private val HELP = "help"

    private fun synthesise(code: String) {
        val results: List<String> = StdlibSynthesizingModel().synthesize(code, 100).toList()

        for (result in results) {
            println("\n---------- BEGIN PROGRAM  ----------")
            print(result)
        }
        print("\n") // don't have next console prompt start on final line of code output.
    }

    @JvmStatic
    fun main(args: Array<String>) {
        /*
         * Define the command line arguments for the application and parse args accordingly.
         */
        val options = Options()
        options.addOption("s", NUM_SAMPLES, true, "the number of asts to sample from the model")
        options.addOption("p", NUM_PROGRAMS, true, "the maximum number of programs to return")
        options.addOption(HELP, HELP, false, "print this message")

        val line = DefaultParser().parse(options, args)

        /*
         * If more arguments are given than possibly correct or the user asked for help, show help message and exit.
         */
        if (args.size >= 6 || line.hasOption(HELP)) {
            val formatter = HelpFormatter()
            formatter.printHelp("synthesize.sh [OPTION]... [FILE]", options)
            System.exit(1)
        }

        /*
         * Determine the query code to synthesize against.
         */
        val code = if (args.isEmpty()) {
            _testDialog
        } else {
            val finalArg = args[args.size - 1]
            if (finalArg.startsWith("-")) {
                System.err.println("If command line arguments are specified, final argument must be a file path.")
                System.exit(2)
            }

            String(Files.readAllBytes(Paths.get(finalArg)))
        }

        /*
         * Determine the model sample count requrest, or null if a default should be used.
         */
        var sampleCount: Int? = null
        if (line.hasOption(NUM_SAMPLES)) {
            val numSamplesString = line.getOptionValue(NUM_SAMPLES)
            try {
                sampleCount = Integer.parseInt(numSamplesString)
                if (sampleCount < 1)
                    throw NumberFormatException()
            } catch (e: NumberFormatException) {
                System.err.println(NUM_SAMPLES + " must be a natural number.")
                System.exit(3)
            }

        }

        var maxProgramCount = Integer.MAX_VALUE
        if (line.hasOption(NUM_PROGRAMS)) {
            val maxProgramCountStr = line.getOptionValue(NUM_PROGRAMS)
            try {
                maxProgramCount = Integer.parseInt(maxProgramCountStr)
                if (maxProgramCount < 1)
                    throw NumberFormatException()
            } catch (e: NumberFormatException) {
                System.err.println(NUM_PROGRAMS + " must be a natural number.")
                System.exit(4)
            }

        }

        synthesise(code)

    }
}
