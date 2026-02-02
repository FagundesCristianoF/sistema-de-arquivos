package operations

class LsOperation(
    private val context: KernelContext,
) : Operation() {
    override fun execute(parameters: String): String {
        var result = ""
        context.log("System call: ls")
        context.log("\tParameters: $parameters")
        when (parameters) {
            "" -> {
                val current = context.newCurrentDirectory()
                current.parseBinary(context.hardDisk.readBlock(context.currentDiskPosition))
                val pointers = context.newPointers(current.getChildrenPointer())
                val children = pointers.parseBinary(context.hardDisk.readBlock(current.getChildrenPointer()))
                for (entry in children) {
                    result += entry
                    result += "\n"
                }
            }

            "-l" -> {
                result = context.listDirectoryDetailed(context.currentDiskPosition)
            }

            else -> {
                result =
                    context.listDirectoryDetailed(
                        context.resolveDirectoryPointer(context.currentDiskPosition, parameters.replace("-l ", "")),
                    )
            }
        }
        return result
    }
}
