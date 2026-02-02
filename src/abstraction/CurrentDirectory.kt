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
    private var name: String = ""
    private var date: String = ""
    private var permission = "-"
    private var parent = 0
    private var childrenPointer = 0
    private var childrenPointerPosition = 0
    private var parentPosition = 0
    private var currentPosition = 0

    init {
        if (name != null && parent != null) {
            this.parent = parent
            this.name = name
            val now = Date()
            this.date = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault()).format(now)
            this.permission = "-rx-r--r--"
            currentPosition = spaceManager.getFreePosition()
            if (name == "/") {
                this.parentPosition = 0
            } else {
                this.parentPosition = parent
            }
            hardDisk.writeBlock(generateBinary(), currentPosition)
            createChildrenPointerBlock()
        }
    }

    fun getName(): String = name

    fun getDate(): String = date

    fun getPermission(): String = permission

    fun getParent(): Int = parent

    fun getChildrenPointer(): Int = childrenPointer

    fun parseBinary(binary: String) {
        var i = fsConstants.NAME_BITS_START
        while (i < fsConstants.NAME_BITS_END) {
            setName(this.name + (Integer.parseInt(binary.substring(i, i + 8), 2)).toChar())
            i += 8
        }
        this.name = this.name.replace(0.toChar().toString(), "")
        setDate(this.date + binary.toDateString(fsConstants))
        permission =
            binaryFormat.applyPermissionBits(
                permission,
                binary.substring(fsConstants.PERMISSION_BITS_START, fsConstants.PERMISSION_BITS_END),
            )
        setParent(Integer.parseInt(binary.substring(fsConstants.PARENT_POINTER_START, fsConstants.PARENT_POINTER_END), 2))
        setChildrenPointer(Integer.parseInt(binary.substring(fsConstants.CHILD_POINTER_START, fsConstants.CHILD_POINTER_END), 2))
    }

    fun setName(name: String) {
        this.name = name
    }

    fun setDate(date: String) {
        this.date = date
    }

    fun setPermission(permission: String) {
        this.permission = permission
    }

    fun updatePermission(position: Int) {
        permissionUtils.updatePermissionAt(position, permission)
    }

    fun setParent(parent: Int) {
        this.parent = parent
    }

    fun setChildrenPointer(childrenPointer: Int) {
        this.childrenPointer = childrenPointer
    }

    fun generateBinary(): String {
        val abstraction = StringBuilder()
        abstraction.append("00")
        abstraction.append(binaryFormat.encodeName(name, fsConstants.NAME_MAX_CHARS, fsConstants.NAME_BITS_END))
        abstraction.append(binaryFormat.encodeDateBits(date))
        abstraction.append(binaryFormat.encodePermissionBits(permission))
        abstraction.append(binaryFormat.padBinary(Integer.toBinaryString(getParentPosition()), fsConstants.POINTER_BITS))
        setChildrenPointerPosition(spaceManager.getFreePosition())
        abstraction.append(
            binaryFormat.padBinary(
                Integer.toBinaryString(getChildrenPointerPosition()),
                fsConstants.POINTER_BITS,
            ),
        )
        val dir =
            CurrentDirectory(
                hardDisk,
                spaceManager,
                fsConstants,
                binaryFormat,
                permissionUtils,
                content,
                logger,
            )
        dir.parseBinary(abstraction.toString())
        return abstraction.toString()
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
        pointers.setParent(getParentPosition())
        hardDisk.writeBlock(pointers.generateBinary(), getChildrenPointerPosition())
    }

    fun getChildrenPointerPosition(): Int = childrenPointerPosition

    fun setChildrenPointerPosition(childrenPointerPosition: Int) {
        this.childrenPointerPosition = childrenPointerPosition
    }

    fun getParentPosition(): Int = parentPosition

    fun getCurrentPosition(): Int = currentPosition
}
