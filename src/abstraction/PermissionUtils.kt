package abstraction

import hardware.HardDisk

interface PermissionUtils {
    fun updatePermissionAt(
        position: Int,
        permission: String,
    )
}

class DefaultPermissionUtils(
    private val hardDisk: HardDisk,
    private val binaryFormat: BinaryFormat,
    private val fsConstants: FsConstants,
) : PermissionUtils {
    override fun updatePermissionAt(
        position: Int,
        permission: String,
    ) {
        val permissionBits = binaryFormat.encodePermissionBits(permission)
        val start =
            fsConstants.DATA_REGION_START +
                (position * fsConstants.BLOCK_SIZE_BITS) +
                fsConstants.PERMISSION_BITS_START
        var k = 0
        for (i in start until start + fsConstants.PERMISSION_BITS_LENGTH) {
            if (permissionBits[k] == '1') {
                hardDisk.setBitAtPosition(true, i)
            } else {
                hardDisk.setBitAtPosition(false, i)
            }
            k++
        }
    }
}
