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
    var name: String = ""
    var date: String = ""
    var permission: String = "-"
    var parent: Int = 0
    var contentPointer: Int = 0
    var contentText: String = ""
    var currentPosition: Int = 0

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
        this.date = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault()).format(Date())
        this.permission = "-rx-r--r--"
        this.contentText = contentText
        this.parent = parent
    }

    fun updatePermission(position: Int) {
        permissionUtils.updatePermissionAt(position, permission)
    }

    fun parseBinary(binary: String) {
        var i = fsConstants.NAME_BITS_START
        while (i < fsConstants.NAME_BITS_END) {
            name += binary.substring(i, i + 8).toInt(2).toChar()
            i += 8
        }
        name = name.replace(0.toChar().toString(), "").replace("null", "")
        date += binary.toDateString(fsConstants)
        permission =
            binaryFormat.applyPermissionBits(
                permission,
                binary.substring(fsConstants.PERMISSION_BITS_START, fsConstants.PERMISSION_BITS_END),
            )
        parent = binary.substring(fsConstants.PARENT_POINTER_START, fsConstants.PARENT_POINTER_END).toInt(2)
        contentPointer = binary.substring(fsConstants.CHILD_POINTER_START, fsConstants.CHILD_POINTER_END).toInt(2)
    }

    fun generateBinary(): String =
        buildString {
            append("01")
            append(binaryFormat.encodeName(name, fsConstants.NAME_MAX_CHARS, fsConstants.NAME_BITS_END))
            append(binaryFormat.encodeDateBits(date))
            append(binaryFormat.encodePermissionBits(permission))
            append(binaryFormat.padBinary(parent.toString(2), fsConstants.POINTER_BITS))
            val contentPosition = content.generateBinary(contentText)
            append(binaryFormat.padBinary(contentPosition.toString(2), fsConstants.POINTER_BITS))
        }
}
