package operations

class CatOperation(
    private val context: KernelContext,
) : Operation() {
    override fun execute(parameters: String): String {
        var params = parameters
        var result = ""
        context.log("System call: cat")
        context.log("\tParameters: $params")
        if (params[0] == '/') {
            params = params.replaceFirst("/", "~/")
        }
        val aux = params.split("/")
        val path = StringBuilder()
        for (i in 0 until aux.size - 1) {
            path.append(aux[i])
            path.append("/")
        }
        context.log("Path " + path.toString())
        val directoryPointer = context.resolveDirectoryPointer(context.currentDiskPosition, path.toString())
        val current = context.newCurrentDirectory()
        current.parseBinary(context.hardDisk.readBlock(directoryPointer))
        val pointers = context.newPointers(current.childrenPointer)
        val children = pointers.parseBinary(context.hardDisk.readBlock(current.childrenPointer))
        for (i in children.indices) {
            val split = children[i].split("-")
            if (split[0] == aux[aux.size - 1]) {
                val file = context.newFileEntry()
                file.parseBinary(context.hardDisk.readBlock(split[1].toInt()))
                result = context.content.parseBinary(context.hardDisk.readBlock(file.contentPointer))
                break
            }
        }
        return result
    }
}
