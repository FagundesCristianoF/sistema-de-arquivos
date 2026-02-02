package operations

class MkdirOperation(
    private val context: KernelContext,
) : Operation() {
    override fun execute(parameters: String): String {
        var params = parameters
        var result = ""
        if (params != "") {
            if (params[0] == '/') {
                params = params.replaceFirst("/", "~/")
            }
            val parts = params.split("/")
            var directoryPointer = context.currentDiskPosition
            var name = params
            if (parts.size > 1) {
                var path = ""
                for (i in 0 until parts.size - 1) {
                    path += parts[i] + "/"
                }
                name = parts[parts.size - 1]
                directoryPointer = context.resolveDirectoryPointer(directoryPointer, path)
            }
            var exists = false
            val current = context.newCurrentDirectory()
            current.parseBinary(context.hardDisk.readBlock(directoryPointer))
            val pointers = context.newPointers(current.getChildrenPointer())
            val children = pointers.parseBinary(context.hardDisk.readBlock(current.getChildrenPointer()))
            for (entry in children) {
                context.log(entry)
            }
            for (i in children.indices) {
                val split = children[i].split("-")
                if (split[0] == name) {
                    exists = true
                    result = "Directory already exists"
                }
            }
            if (!exists) {
                val child = context.newCurrentDirectory(name, directoryPointer)
                val positionToAdd = child.getCurrentPosition()
                val parent = context.newCurrentDirectory()
                parent.parseBinary(context.hardDisk.readBlock(directoryPointer))
                val parentPointers = context.newPointers(parent.getChildrenPointer())
                result = parentPointers.addChild(positionToAdd)
            }
        }
        return result
    }
}
