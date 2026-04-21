package abstraction

import hardware.HardDisk
import infra.Logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CurrentDirectory(
    private val hardDisk: HardDisk,
    private val spaceManager: SpaceManager,
    private val fsConstants: FsConstants,
    private val binaryFormat: BinaryFormat,
    private val permissionUtils: PermissionUtils,
    private val content: Content,
    private val logger: Logger,
    name: String? = null,
    parent: Int? = null,
) {
    var name: String = ""
    var date: String = ""
    var permission: String = "-"
    var parent: Int = 0
    var childrenPointer: Int = 0
    var childrenPointerPosition: Int = 0
    var parentPosition: Int = 0
    var currentPosition: Int = 0

    init {
        if (name != null && parent != null) {
            this.parent = parent
            this.name = name
            this.date = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault()).format(Date())
            this.permission = "-rx-r--r--"
            currentPosition = spaceManager.getFreePosition()
            parentPosition = if (name == "/") 0 else parent
            hardDisk.writeBlock(generateBinary(), currentPosition)
            createChildrenPointerBlock()
        }
    }

    fun parseBinary(binary: String) {
        var i = fsConstants.NAME_BITS_START
        while (i < fsConstants.NAME_BITS_END) {
            name += binary.substring(i, i + 8).toInt(2).toChar()
            i += 8
        }
        name = name.replace(0.toChar().toString(), "")
        date += binary.toDateString(fsConstants)
        permission =
            binaryFormat.applyPermissionBits(
                permission,
                binary.substring(fsConstants.PERMISSION_BITS_START, fsConstants.PERMISSION_BITS_END),
            )
        parent = binary.substring(fsConstants.PARENT_POINTER_START, fsConstants.PARENT_POINTER_END).toInt(2)
        childrenPointer = binary.substring(fsConstants.CHILD_POINTER_START, fsConstants.CHILD_POINTER_END).toInt(2)
    }

    fun updatePermission(position: Int) {
        permissionUtils.updatePermissionAt(position, permission)
    }

    fun generateBinary(): String =
        buildString {
            append("00")
            append(binaryFormat.encodeName(name, fsConstants.NAME_MAX_CHARS, fsConstants.NAME_BITS_END))
            append(binaryFormat.encodeDateBits(date))
            append(binaryFormat.encodePermissionBits(permission))
            append(binaryFormat.padBinary(parentPosition.toString(2), fsConstants.POINTER_BITS))
            childrenPointerPosition = spaceManager.getFreePosition()
            append(binaryFormat.padBinary(childrenPointerPosition.toString(2), fsConstants.POINTER_BITS))
            val dir = CurrentDirectory(hardDisk, spaceManager, fsConstants, binaryFormat, permissionUtils, content, logger)
            dir.parseBinary(toString())
        }

    private fun createChildrenPointerBlock() {
        val pointers =
            Pointers(
                childrenPointerPosition,
                hardDisk,
                fsConstants,
                spaceManager,
                binaryFormat,
                permissionUtils,
                content = content,
                logger = logger,
            )
        pointers.parent = parentPosition
        hardDisk.writeBlock(pointers.generateBinary(), childrenPointerPosition)
    }
}
