package operations

import abstraction.BinaryFormat
import abstraction.Content
import abstraction.CurrentDirectory
import abstraction.FileEntry
import abstraction.FsConstants
import abstraction.PermissionUtils
import abstraction.Pointers
import abstraction.SpaceManager
import binary.Binary
import hardware.HardDisk
import infra.Logger

interface KernelContext {
    var currentDiskPosition: Int
    val logger: Logger
    val hardDisk: HardDisk
    val spaceManager: SpaceManager
    val fsConstants: FsConstants
    val binaryFormat: BinaryFormat
    val permissionUtils: PermissionUtils
    val content: Content
    val binary: Binary

    fun log(message: Any)

    fun newCurrentDirectory(): CurrentDirectory

    fun newCurrentDirectory(
        name: String,
        parent: Int,
    ): CurrentDirectory

    fun newFileEntry(): FileEntry

    fun newFileEntry(
        name: String,
        contentText: String,
        parent: Int,
    ): FileEntry

    fun newPointers(currentPosition: Int): Pointers

    fun listDirectoryDetailed(directoryPointer: Int): String

    fun findChildDirectoryPointer(
        directoryPointer: Int,
        name: String,
    ): Int

    fun currentPath(directoryPointer: Int): String

    fun resolveDirectoryPointer(
        directoryPointer: Int,
        path: String,
    ): Int

    fun fileDoesNotExist(
        children: ArrayList<String>,
        name: String,
    ): Boolean

    fun dumpDirectory(
        directoryPointer: Int,
        output: StringBuilder,
    )
}

class DefaultKernelContext(
    override val logger: Logger,
    override val hardDisk: HardDisk,
    override val spaceManager: SpaceManager,
    override val fsConstants: FsConstants,
    override val binaryFormat: BinaryFormat,
    override val permissionUtils: PermissionUtils,
    override val content: Content,
    override val binary: Binary,
) : KernelContext {
    override var currentDiskPosition: Int = 0

    init {
        CurrentDirectory(
            hardDisk,
            spaceManager,
            fsConstants,
            binaryFormat,
            permissionUtils,
            content,
            logger,
            name = "/",
            parent = 0,
        )
    }

    override fun log(message: Any) {
        logger.info(message.toString())
    }

    override fun newCurrentDirectory(): CurrentDirectory =
        CurrentDirectory(
            hardDisk,
            spaceManager,
            fsConstants,
            binaryFormat,
            permissionUtils,
            content,
            logger,
        )

    override fun newCurrentDirectory(
        name: String,
        parent: Int,
    ): CurrentDirectory =
        CurrentDirectory(
            hardDisk,
            spaceManager,
            fsConstants,
            binaryFormat,
            permissionUtils,
            content,
            logger,
            name = name,
            parent = parent,
        )

    override fun newFileEntry(): FileEntry =
        FileEntry(
            binaryFormat,
            fsConstants,
            permissionUtils,
            content,
        )

    override fun newFileEntry(
        name: String,
        contentText: String,
        parent: Int,
    ): FileEntry =
        FileEntry(
            binaryFormat,
            fsConstants,
            permissionUtils,
            content,
            name,
            contentText,
            parent,
        )

    override fun newPointers(currentPosition: Int): Pointers =
        Pointers(
            currentPosition,
            hardDisk,
            fsConstants,
            spaceManager,
            binaryFormat,
            permissionUtils,
            content,
            logger,
        )

    override fun listDirectoryDetailed(directoryPointer: Int): String {
        val output = StringBuilder()
        val current = newCurrentDirectory()
        current.parseBinary(hardDisk.readBlock(directoryPointer))
        val pointers = newPointers(current.getChildrenPointer())
        val children = pointers.parseBinary(hardDisk.readBlock(current.getChildrenPointer()))
        for (i in children.indices) {
            val split = children[i].split("-")
            if (split[0].contains(".txt")) {
                output.append(split[0])
                val file = newFileEntry()
                file.parseBinary(hardDisk.readBlock(split[1].toInt()))
                output.append(" ")
                output.append(file.getPermission())
                output.append(" ")
                output.append(file.getDate())
                output.append("\n")
            } else {
                output.append(split[0])
                val directory = newCurrentDirectory()
                directory.parseBinary(hardDisk.readBlock(split[1].toInt()))
                output.append(" ")
                output.append(directory.getPermission())
                output.append(" ")
                output.append(directory.getDate())
                output.append("\n")
            }
        }
        return output.toString()
    }

    override fun findChildDirectoryPointer(
        directoryPointer: Int,
        name: String,
    ): Int {
        var found = -1
        val current = newCurrentDirectory()
        current.parseBinary(hardDisk.readBlock(directoryPointer))
        val childrenPointer = current.getChildrenPointer()
        val pointers = newPointers(childrenPointer)
        val children = pointers.parseBinary(hardDisk.readBlock(childrenPointer))
        for (i in children.indices) {
            val split = children[i].split("-")
            val cleanName = split[0].replace(0.toChar().toString(), "")
            if (cleanName == name) {
                found = split[1].toInt()
                break
            }
        }
        return found
    }

    override fun currentPath(directoryPointer: Int): String {
        if (directoryPointer != 0) {
            val current = newCurrentDirectory()
            current.parseBinary(hardDisk.readBlock(directoryPointer))
            return currentPath(current.getParent()) + "/" + current.getName()
        }
        return ""
    }

    override fun resolveDirectoryPointer(
        directoryPointer: Int,
        path: String,
    ): Int {
        var currentPointer = directoryPointer
        var localPath = path
        if (localPath[0] == '/') {
            localPath = localPath.replaceFirst("/", "~/")
            log(localPath)
        }
        val parts = localPath.split("/")
        var current: CurrentDirectory
        for (i in parts.indices) {
            when (parts[i]) {
                ".." -> {
                    current = newCurrentDirectory()
                    current.parseBinary(hardDisk.readBlock(currentPointer))
                    currentPointer = current.getParent()
                }

                "." -> {
                    currentPointer = currentPointer
                }

                "~" -> {
                    currentPointer = 0
                }

                else -> {
                    val nextPointer = findChildDirectoryPointer(currentPointer, parts[i])
                    if (nextPointer >= 0) {
                        currentPointer = nextPointer
                    }
                }
            }
        }
        return currentPointer
    }

    override fun fileDoesNotExist(
        children: ArrayList<String>,
        name: String,
    ): Boolean {
        var result = true
        for (i in children.indices) {
            val split = children[i].split("-")
            if (split[0] == name) {
                result = false
                break
            }
        }
        return result
    }

    override fun dumpDirectory(
        directoryPointer: Int,
        output: StringBuilder,
    ) {
        val current = newCurrentDirectory()
        current.parseBinary(hardDisk.readBlock(directoryPointer))
        log("At " + current.getName())
        val pointers = newPointers(current.getChildrenPointerPosition())
        log("Children pointer " + current.getChildrenPointerPosition())
        val children = pointers.parseBinary(hardDisk.readBlock(current.getChildrenPointerPosition()))
        for (i in 1 until children.size) {
            log("Children")
            val split = children[i].split("-")
            if (split[0].contains(".txt")) {
                output.append("createfile ")
                output.append("./")
                output.append(split[0])
                output.append(" ")
                val file = newFileEntry()
                file.parseBinary(hardDisk.readBlock(split[1].toInt()))
                output.append(content.parseBinary(hardDisk.readBlock(file.getContentPointer())))
            } else {
                output.append("mkdir ")
                output.append(split[0])
                output.append("\n")
                dumpDirectory(split[1].toInt(), output)
            }
        }
    }
}
