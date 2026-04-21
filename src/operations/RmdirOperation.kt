package operations

class RmdirOperation(
    private val context: KernelContext,
) : Operation() {
    override fun execute(parameters: String): String {
        var params = parameters
        var result = ""
        context.log("System call: rmdir")
        context.log("\tParameters: $params")
        params = params.replace("-r ", "")
        context.log("\tParameters: $params")
        if (params[0] == '/') {
            params = params.replaceFirst("/", "~/")
        }
        val directories = params.split("/")
        val directoryPointer = context.resolveDirectoryPointer(context.currentDiskPosition, params)
        val current = context.newCurrentDirectory()
        current.parseBinary(context.hardDisk.readBlock(directoryPointer))
        context.log("Parent name " + current.name)
        val targetName = directories[directories.size - 1].replace(" ", "")
        context.log("Looking for $targetName")
        val pointers = context.newPointers(current.childrenPointer)
        val children = pointers.parseBinary(context.hardDisk.readBlock(current.childrenPointer))
        if (children.size == 1) {
            val parentPointer = pointers.getParent()
            val parent = context.newCurrentDirectory()
            parent.parseBinary(context.hardDisk.readBlock(parentPointer))
            val grandParent = context.newCurrentDirectory()
            grandParent.parseBinary(context.hardDisk.readBlock(parent.parent))
            val parentPointers = context.newPointers(grandParent.childrenPointer)
            val parentChildren = parentPointers.parseBinary(context.hardDisk.readBlock(grandParent.childrenPointer))
            var removed = false
            var i = 1
            while (i < parentChildren.size) {
                val split = parentChildren[i].split("-")
                context.log("Names " + split[0])
                context.log("Comparing with " + directories[directories.size - 1])
                if (split[0] == targetName) {
                    context.hardDisk.setBitAtPosition(false, split[1].toInt())
                    context.log("Position " + split[2])
                    parentPointers.setUsedPosition(false, split[2].toInt())
                    removed = true
                    i = parentChildren.size
                } else {
                    result = "Directory does not exist"
                }
                i++
            }
            if (removed) {
                result = ""
            }
        } else {
            result = "Directory contains files and/or subdirectories"
        }
        return result
    }
}
