package abstraction

import hardware.HardDisk

interface SpaceManager {
    fun getFreePosition(): Int

    fun clearPosition(i: Int)
}

class DefaultSpaceManager(
    private val hardDisk: HardDisk,
    private val fsConstants: FsConstants,
) : SpaceManager {
    override fun getFreePosition(): Int {
        for (i in 0 until fsConstants.BITMAP_BITS) {
            if (!hardDisk.getBitAtPosition(i)) {
                hardDisk.setBitAtPosition(true, i)
                return i
            }
        }
        return -1
    }

    override fun clearPosition(i: Int) {
        hardDisk.setBitAtPosition(false, i)
    }
}
