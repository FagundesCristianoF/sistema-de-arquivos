package hardware

import abstraction.FsConstants

interface HardDisk {
    fun initializeSecondaryMemory()

    fun setBitAtPosition(
        bit: Boolean,
        position: Int,
    )

    fun getBitAtPosition(i: Int): Boolean

    fun readBlock(i: Int): String

    fun writeBlock(
        block: String,
        i: Int,
    )

    fun writePointer(
        pointer: String,
        i: Int,
    )
}

class DefaultHardDisk(
    private val fsConstants: FsConstants,
) : HardDisk {
    private val numberOfBits: Int = fsConstants.DISK_TOTAL_BITS
    private val hardDisk: BooleanArray = BooleanArray(numberOfBits)

    override fun initializeSecondaryMemory() {
        for (i in 0 until numberOfBits) {
            hardDisk[i] = false
        }
    }

    override fun setBitAtPosition(
        bit: Boolean,
        position: Int,
    ) {
        hardDisk[position] = bit
    }

    override fun getBitAtPosition(i: Int): Boolean = hardDisk[i]

    override fun readBlock(i: Int): String {
        val block = StringBuilder()
        val start = (i * fsConstants.BLOCK_SIZE_BITS) + fsConstants.DATA_REGION_START
        for (j in start until start + fsConstants.BLOCK_SIZE_BITS) {
            if (hardDisk[j]) {
                block.append("1")
            } else {
                block.append("0")
            }
        }
        return block.toString()
    }

    override fun writeBlock(
        block: String,
        i: Int,
    ) {
        val start = fsConstants.DATA_REGION_START + (i * fsConstants.BLOCK_SIZE_BITS)
        var k = 0
        for (j in start until start + fsConstants.BLOCK_SIZE_BITS) {
            if (block[k] == '1') {
                setBitAtPosition(true, j)
            } else {
                setBitAtPosition(false, j)
            }
            k++
        }
    }

    override fun writePointer(
        pointer: String,
        i: Int,
    ) {
        for ((k, j) in (i until i + fsConstants.POINTER_BITS).withIndex()) {
            if (pointer[k] == '1') {
                setBitAtPosition(true, j)
            } else {
                setBitAtPosition(false, j)
            }
        }
    }
}
