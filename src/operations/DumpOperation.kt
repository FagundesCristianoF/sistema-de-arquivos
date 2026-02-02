package operations

import java.io.BufferedWriter
import java.io.FileWriter
import java.io.IOException
import java.io.File as IoFile

class DumpOperation(
    private val context: KernelContext,
) : Operation() {
    override fun execute(parameters: String): String {
        context.log("System call: dump")
        context.log("\tParameters: $parameters")
        val output = StringBuilder()
        context.dumpDirectory(0, output)
        context.log(output.toString())
        val file = IoFile(parameters)
        if (file.exists()) {
            var writer: BufferedWriter? = null
            try {
                context.log("Existing file")
                writer = BufferedWriter(FileWriter(file, false))
                writer.write(output.toString())
                writer.flush()
                writer.close()
                context.log(output.toString())
            } catch (ex: IOException) {
                context.logger.error("Dump failed", ex)
            } finally {
                try {
                    writer?.close()
                } catch (ex: IOException) {
                    context.logger.error("Failed to close dump writer", ex)
                }
            }
        } else {
            return "File does not exist."
        }
        return ""
    }
}
