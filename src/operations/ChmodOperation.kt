package operations

class ChmodOperation(
    private val context: KernelContext,
) : Operation() {
    override fun execute(parameters: String): String {
        var result = ""
        context.log("System call: chmod")
        context.log("\tParameters: $parameters")
        val args = parameters.split(" ")
        val permissionBits =
            if (args.size == 2) {
                args[0]
            } else {
                args[1]
            }
        val permissionString =
            buildString {
                append("-")
                for (i in 0..2) {
                    append(
                        when (permissionBits[i]) {
                            '7' -> "rwx"
                            '6' -> "rw-"
                            '5' -> "r-x"
                            '4' -> "r--"
                            '3' -> "-wx"
                            '2' -> "-w-"
                            '1' -> "--x"
                            else -> "---"
                        },
                    )
                }
            }
        context.log(permissionString)
        if (args.size == 2) {
            val pathParts = args[1].split("/")
            var path = ""
            for (i in 0 until pathParts.size - 1) {
                path = "/" + pathParts[i]
            }
            val directoryPointer = context.resolveDirectoryPointer(context.currentDiskPosition, path)
            val current = context.newCurrentDirectory()
            current.parseBinary(context.hardDisk.readBlock(directoryPointer))
            val pointers = context.newPointers(current.childrenPointer)
            val children = pointers.parseBinary(context.hardDisk.readBlock(current.childrenPointer))
            for (i in children.indices) {
                val split = children[i].split("-")
                if (split[0] == pathParts[pathParts.size - 1]) {
                    if (split[0].contains(".txt")) {
                        val file = context.newFileEntry()
                        file.parseBinary(context.hardDisk.readBlock(split[1].toInt()))
                        file.currentPosition = split[1].toInt()
                        file.permission = permissionString
                        file.updatePermission(split[1].toInt())
                    } else {
                        val directory = context.newCurrentDirectory()
                        directory.parseBinary(context.hardDisk.readBlock(split[1].toInt()))
                        context.log("Directory name ${directory.name}")
                        directory.permission = permissionString
                        directory.updatePermission(split[1].toInt())
                    }
                }
            }
        }
        return result
    }
}
