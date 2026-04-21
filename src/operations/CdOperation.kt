package operations

import operatingSystem.fileSystem.FileSystemSimulator

class CdOperation(
    private val context: KernelContext,
) : Operation() {
    override fun execute(parameters: String): String {
        var result = ""
        var current: abstraction.CurrentDirectory
        var positionAux = context.currentDiskPosition
        context.log(parameters)
        when (parameters) {
            ".." -> {
                current = context.newCurrentDirectory()
                current.parseBinary(context.hardDisk.readBlock(context.currentDiskPosition))
                positionAux = current.parent
            }

            "/" -> {
                positionAux = 0
            }

            "" -> {
                result = "Invalid path"
            }

            else -> {
                positionAux = context.resolveDirectoryPointer(positionAux, parameters)
            }
        }
        context.log("Previous position ${context.currentDiskPosition}")
        context.log("New position $positionAux")
        if (positionAux == context.currentDiskPosition && parameters != "/") {
            result = "Directory does not exist"
        }
        context.currentDiskPosition = positionAux
        val currentPath: String = context.currentPath(context.currentDiskPosition) + "/"
        context.log("Directory: $currentPath")
        FileSystemSimulator.currentDir = currentPath
        return result
    }
}
