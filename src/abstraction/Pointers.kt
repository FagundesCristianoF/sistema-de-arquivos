package abstraction

import hardware.HardDisk
import infra.Logger

class Pointers(
    private val currentPosition: Int,
    private val hardDisk: HardDisk,
    private val fsConstants: FsConstants,
    private val spaceManager: SpaceManager,
    private val binaryFormat: BinaryFormat,
    private val permissionUtils: PermissionUtils,
    private val content: Content,
    private val logger: Logger,
) {
    var parent = 0
    var hasContinuation = false
    var nextPointer = 0
    private val usedSlots = BooleanArray(fsConstants.POINTERS_COUNT)
    private val children = IntArray(fsConstants.POINTERS_COUNT)

    fun parseBinary(binary: String): MutableList<String> {
        val childrenList = mutableListOf<String>()
        this.parent =
            binary.substring(fsConstants.POINTER_PARENT_START, fsConstants.POINTER_PARENT_END).toInt(2)
        childrenList.add(".." + "-" + this.parent)
        for (i in fsConstants.POINTER_USED_START until fsConstants.POINTER_USED_START + fsConstants.POINTER_USED_COUNT) {
            if (binary[i] == '1') {
                val initial =
                    (i - fsConstants.POINTER_USED_START) * fsConstants.POINTER_BITS +
                        fsConstants.POINTER_CHILDREN_START
                usedSlots[i - fsConstants.POINTER_USED_START] = true
                val position =
                    binary.substring(initial, initial + fsConstants.POINTER_BITS).toInt(2)
                val block = hardDisk.readBlock(position)
                val result = entryNameFromBlock(block, position, i - fsConstants.POINTER_USED_START)
                if (result != null) childrenList.add(result)
            }
        }
        if (hasContinuation) {
            loadMoreChildren(childrenList, nextPointer)
        }
        return childrenList
    }

    fun loadMoreChildren(
        childrenList: MutableList<String>,
        blockIndex: Int,
    ) {
        val nextPointers =
            Pointers(
                blockIndex,
                hardDisk,
                fsConstants,
                spaceManager,
                binaryFormat,
                permissionUtils,
                content,
                logger,
            )
        val binary = hardDisk.readBlock(blockIndex)
        for (i in fsConstants.POINTER_USED_START until fsConstants.POINTER_USED_START + fsConstants.POINTER_USED_COUNT) {
            if (binary[i] == '1') {
                val inicio =
                    (i - fsConstants.POINTER_USED_START) * fsConstants.POINTER_BITS +
                        fsConstants.POINTER_CHILDREN_START
                val position =
                    binary.substring(inicio, inicio + fsConstants.POINTER_BITS).toInt(2)
                val block = hardDisk.readBlock(position)
                val result = entryNameFromBlock(block, position, i - fsConstants.POINTER_USED_START)
                if (result != null) childrenList.add(result)
            }
        }
        val continuationFlagIndex =
            fsConstants.POINTER_CHILDREN_START + fsConstants.POINTERS_COUNT * fsConstants.POINTER_BITS
        nextPointers.hasContinuation = binary[continuationFlagIndex] == '1'
        val nextPtrStart = continuationFlagIndex + 1
        nextPointers.nextPointer =
            binary.substring(nextPtrStart, nextPtrStart + fsConstants.POINTER_BITS).toInt(2)
        if (nextPointers.hasContinuation) {
            loadMoreChildren(childrenList, nextPointers.nextPointer)
        }
    }

    private fun entryNameFromBlock(
        block: String,
        position: Int,
        slotIndex: Int,
    ): String? =
        when (block.take(2)) {
            "00" -> {
                val entry =
                    CurrentDirectory(
                        hardDisk,
                        spaceManager,
                        fsConstants,
                        binaryFormat,
                        permissionUtils,
                        content,
                        logger,
                    )
                entry.parseBinary(block)
                "${entry.name.replace(0.toChar().toString(), "")}-$position-$slotIndex"
            }
            "01" -> {
                val entry = FileEntry(binaryFormat, fsConstants, permissionUtils, content)
                entry.parseBinary(block)
                "${entry.name.replace(0.toChar().toString(), "")}-$position-$slotIndex"
            }
            else -> null
        }

    fun setUsedPosition(
        value: Boolean,
        position: Int,
    ) {
        usedSlots[position] = value
        hardDisk.writeBlock(generateBinary(), currentPosition)
    }

    fun generateBinary(): String =
        buildString {
            append("10")
            append(padBinary(parent.toString(2), fsConstants.POINTER_BITS))
            var usedBits = ""
            var childrenBits = ""
            for (i in 0 until fsConstants.POINTERS_COUNT) {
                if (usedSlots[i]) {
                    usedBits += "1"
                    childrenBits += padBinary(children[i].toString(2), fsConstants.POINTER_BITS)
                } else {
                    usedBits += "0"
                    childrenBits += emptyPointer()
                }
            }
            append(usedBits)
            append(childrenBits)
            if (hasContinuation) {
                append("1")
                append(padBinary(spaceManager.getFreePosition().toString(2), fsConstants.POINTER_BITS))
            } else {
                append("0")
                append(emptyPointer())
            }
        }

    private fun emptyPointer(): String = "0000000000000000"

    private fun padBinary(
        s: String,
        size: Int,
    ): String = s.padStart(size, '0')

    fun addChild(child: Int): String {
        var result = ""
        parseBinary(hardDisk.readBlock(currentPosition))
        var inserted = false
        for (i in 0 until fsConstants.POINTERS_COUNT) {
            if (!usedSlots[i]) {
                usedSlots[i] = true
                inserted = true
                children[i] = child
                hardDisk.writePointer(
                    padBinary(child.toString(2), fsConstants.POINTER_BITS),
                    fsConstants.DATA_REGION_START +
                        (currentPosition * fsConstants.BLOCK_SIZE_BITS) +
                        fsConstants.POINTER_CHILDREN_START +
                        (i * fsConstants.POINTER_BITS),
                )
                hardDisk.setBitAtPosition(
                    true,
                    fsConstants.DATA_REGION_START +
                        (currentPosition * fsConstants.BLOCK_SIZE_BITS) +
                        fsConstants.POINTER_USED_START +
                        i,
                )
                break
            }
        }
        if (!inserted) {
            logger.warn("System full")
            result = "Unable to create directory - system full"
            if (hasContinuation) {
                val next =
                    Pointers(
                        this.nextPointer,
                        hardDisk,
                        fsConstants,
                        spaceManager,
                        binaryFormat,
                        permissionUtils,
                        content,
                        logger,
                    )
                next.addChild(child)
            } else {
                val newNext =
                    Pointers(
                        spaceManager.getFreePosition(),
                        hardDisk,
                        fsConstants,
                        spaceManager,
                        binaryFormat,
                        permissionUtils,
                        content,
                        logger,
                    )
                hasContinuation = true
                this.nextPointer = newNext.getCurrentPosition()
                newNext.parent = parent
                newNext.addChild(child)
            }
        }
        return result
    }

    fun getCurrentPosition(): Int = currentPosition
}
