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
        val permissionString = StringBuilder("-")
        for (i in 0..2) {
            when (permissionBits[i]) {
                '0' -> {
                    permissionString.append("---")
                }

                '1' -> {
                    permissionString.append("--w")
                }

                '2' -> {
                    permissionString.append("-x-")
                }

                '3' -> {
                    permissionString.append("-xw")
                }

                '4' -> {
                    permissionString.append("r--")
                }

                '5' -> {
                    permissionString.append("r-w")
                }

                '6' -> {
                    permissionString.append("rx-")
                }

                '7' -> {
                    permissionString.append("rxw")
                }

                else -> {
                    context.log("Permission error")
                    result = "Permission error"
                }
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
            val pointers = context.newPointers(current.getChildrenPointer())
            val children = pointers.parseBinary(context.hardDisk.readBlock(current.getChildrenPointer()))
            for (i in children.indices) {
                val split = children[i].split("-")
                if (split[0] == pathParts[pathParts.size - 1]) {
                    if (split[0].contains(".txt")) {
                        val file = context.newFileEntry()
                        file.parseBinary(context.hardDisk.readBlock(split[1].toInt()))
                        file.setCurrentPosition(split[1].toInt())
                        file.setPermission(permissionString.toString())
                        file.updatePermission(split[1].toInt())
                    } else {
                        val directory = context.newCurrentDirectory()
                        directory.parseBinary(context.hardDisk.readBlock(split[1].toInt()))
                        context.log("Directory name " + directory.getName())
                        directory.setPermission(permissionString.toString())
                        directory.updatePermission(split[1].toInt())
                    }
                }
            }
        }
        return result
    }
}
