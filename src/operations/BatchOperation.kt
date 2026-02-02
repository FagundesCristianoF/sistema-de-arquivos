package operations

import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException

class BatchOperation(
    private val context: KernelContext,
    private val cdOperation: CdOperation,
    private val mkdirOperation: MkdirOperation,
    private val lsOperation: LsOperation,
    private val createFileOperation: CreateFileOperation,
    private val chmodOperation: ChmodOperation,
    private val rmOperation: RmOperation,
    private val dumpOperation: DumpOperation,
) : Operation() {
    override fun execute(parameters: String): String {
        var result = ""
        context.log("System call: batch")
        context.log("\tParameters: $parameters")
        try {
            val reader = BufferedReader(FileReader(parameters))
            while (true) {
                val line = reader.readLine()
                if (line != null) {
                    val args = line.split(" ")
                    when (args[0]) {
                        "cd" -> {
                            context.log("CD " + line.replace("cd ", ""))
                            cdOperation.execute(line.replace("cd ", ""))
                        }

                        "mkdir" -> {
                            mkdirOperation.execute(line.replace("mkdir ", ""))
                        }

                        "ls" -> {
                            lsOperation.execute(line.replace("ls ", ""))
                        }

                        "createfile" -> {
                            createFileOperation.execute(line.replace("createfile ", ""))
                        }

                        "chmod" -> {
                            chmodOperation.execute(line.replace("chmod", ""))
                        }

                        "rm" -> {
                            rmOperation.execute(line.replace("rm", ""))
                        }

                        "dump" -> {
                            dumpOperation.execute(line.replace("dump", result))
                        }
                    }
                } else {
                    break
                }
            }
        } catch (ex: FileNotFoundException) {
            context.logger.error("Batch file not found", ex)
        } catch (ex: IOException) {
            context.logger.error("Batch execution failed", ex)
        }
        return result
    }
}
