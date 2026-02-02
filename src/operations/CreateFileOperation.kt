package operations

class CreateFileOperation(
    private val context: KernelContext,
) : Operation() {
    override fun execute(parameters: String): String {
        var params = parameters
        var result = ""
        context.log("System call: createfile")
        context.log("\tParameters: $params")
        val parts = params.split(" ").toMutableList()
        val directoryPointer = context.resolveDirectoryPointer(context.currentDiskPosition, parts[0])
        if (parts[0][0] == '/') {
            parts[0] = parts[0].replaceFirst("/", "~/")
        }
        val nameParts = parts[0].split("/")
        val fileName = nameParts[nameParts.size - 1]
        val current = context.newCurrentDirectory()
        current.parseBinary(context.hardDisk.readBlock(directoryPointer))
        val pointers = context.newPointers(current.getChildrenPointer())
        val children = pointers.parseBinary(context.hardDisk.readBlock(current.getChildrenPointer()))
        if (context.fileDoesNotExist(children, fileName)) {
            val contentBuilder = StringBuilder()
            for (i in 1 until parts.size) {
                contentBuilder.append(parts[i])
                contentBuilder.append(" ")
            }
            var finalContent = ""
            var i = 0
            while (i < contentBuilder.length) {
                if (contentBuilder[i] != '\\') {
                    finalContent += contentBuilder[i]
                } else {
                    if (contentBuilder[i + 1] == 'n') {
                        finalContent += "\n"
                        i++
                    } else {
                        finalContent += "\\"
                    }
                }
                i++
            }
            val file = context.newFileEntry(fileName, finalContent, current.getCurrentPosition())
            val filePosition = context.spaceManager.getFreePosition()
            context.hardDisk.writeBlock(file.generateBinary(), filePosition)
            pointers.addChild(filePosition)
        } else {
            result = "File already exists."
        }
        return result
    }
}
