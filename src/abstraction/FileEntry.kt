package abstraction

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileEntry(
    private val binaryFormat: BinaryFormat,
    private val fsConstants: FsConstants,
    private val permissionUtils: PermissionUtils,
    private val content: Content,
) {
    private var name: String = ""
    private var date: String = ""
    private var permission = "-"
    private var parent = 0
    private var contentPointer = 0
    private var contentText: String = ""
    private var currentPosition = 0

    constructor(
        binaryFormat: BinaryFormat,
        fsConstants: FsConstants,
        permissionUtils: PermissionUtils,
        content: Content,
        name: String,
        contentText: String,
        parent: Int,
    ) : this(binaryFormat, fsConstants, permissionUtils, content) {
        this.name = name
        val now = Date()
        this.date = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault()).format(now)
        this.permission = "-rx-r--r--"
        this.contentText = contentText
        this.parent = parent
    }

    fun getName(): String = name

    fun getDate(): String = date

    fun getPermission(): String = permission

    fun getParent(): Int = parent

    fun getContentPointer(): Int = contentPointer

    fun getCurrentPosition(): Int = currentPosition

    fun setCurrentPosition(currentPosition: Int) {
        this.currentPosition = currentPosition
    }

    fun parseBinary(binary: String) {
        var i = fsConstants.NAME_BITS_START
        while (i < fsConstants.NAME_BITS_END) {
            this.name += (Integer.parseInt(binary.substring(i, i + 8), 2)).toChar()
            i += 8
        }
        this.name = this.name.replace(0.toChar().toString(), "").replace("null", "")
        this.date += binary.toDateString(fsConstants)
        permission =
            binaryFormat.applyPermissionBits(
                permission,
                binary.substring(fsConstants.PERMISSION_BITS_START, fsConstants.PERMISSION_BITS_END),
            )
        this.parent = Integer.parseInt(binary.substring(fsConstants.PARENT_POINTER_START, fsConstants.PARENT_POINTER_END), 2)
        this.contentPointer = Integer.parseInt(binary.substring(fsConstants.CHILD_POINTER_START, fsConstants.CHILD_POINTER_END), 2)
    }

    fun setPermission(permission: String) {
        this.permission = permission
    }

    fun updatePermission(position: Int) {
        permissionUtils.updatePermissionAt(position, permission)
    }

    fun generateBinary(): String {
        val abstraction = StringBuilder()
        abstraction.append("01")
        abstraction.append(binaryFormat.encodeName(name, fsConstants.NAME_MAX_CHARS, fsConstants.NAME_BITS_END))
        abstraction.append(binaryFormat.encodeDateBits(date))
        abstraction.append(binaryFormat.encodePermissionBits(permission))
        abstraction.append(binaryFormat.padBinary(Integer.toBinaryString(parent), fsConstants.POINTER_BITS))
        val contentPosition = content.generateBinary(contentText)
        abstraction.append(binaryFormat.padBinary(Integer.toBinaryString(contentPosition), fsConstants.POINTER_BITS))
        return abstraction.toString()
    }
}
