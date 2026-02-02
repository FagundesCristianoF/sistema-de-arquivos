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
    private var parent = 0
    private var hasMore = false
    private var nextPointer = 0
    private val usedSlots = BooleanArray(fsConstants.POINTERS_COUNT)
    private val children = IntArray(fsConstants.POINTERS_COUNT)

    fun getParent(): Int = parent

    fun hasContinuation(): Boolean = hasMore

    fun getNextPointer(): Int = nextPointer

    fun parseBinary(binary: String): ArrayList<String> {
        val childrenList = ArrayList<String>()
        this.parent =
            Integer.parseInt(
                binary.substring(fsConstants.POINTER_PARENT_START, fsConstants.POINTER_PARENT_END),
                2,
            )
        childrenList.add(".." + "-" + this.parent)
        for (i in fsConstants.POINTER_USED_START until fsConstants.POINTER_USED_START + fsConstants.POINTER_USED_COUNT) {
            if (binary[i] == '1') {
                val initial =
                    (i - fsConstants.POINTER_USED_START) * fsConstants.POINTER_BITS +
                        fsConstants.POINTER_CHILDREN_START
                usedSlots[i - fsConstants.POINTER_USED_START] = true
                val position =
                    Integer.parseInt(
                        binary.substring(initial, initial + fsConstants.POINTER_BITS),
                        2,
                    )
                val block = hardDisk.readBlock(position)
                if (block.subSequence(0, 2) == "00") {
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
                    val result =
                        entry.getName().replace(0.toChar().toString(), "") +
                            "-" + position + "-" + (i - fsConstants.POINTER_USED_START)
                    childrenList.add(result)
                }
                if (block.subSequence(0, 2) == "01") {
                    val entry =
                        FileEntry(
                            binaryFormat,
                            fsConstants,
                            permissionUtils,
                            content,
                        )
                    entry.parseBinary(block)
                    val result =
                        entry.getName().replace(0.toChar().toString(), "") +
                            "-" + position + "-" + (i - fsConstants.POINTER_USED_START)
                    childrenList.add(result)
                }
            }
        }
        if (hasMore) {
            loadMoreChildren(childrenList, nextPointer)
        }
        return childrenList
    }

    fun loadMoreChildren(
        childrenList: ArrayList<String>,
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
                nextPointers.getUsedSlots()[i - fsConstants.POINTER_USED_START] = true
                val position =
                    Integer.parseInt(
                        binary.substring(inicio, inicio + fsConstants.POINTER_BITS),
                        2,
                    )
                val block = hardDisk.readBlock(position)
                if (block.subSequence(0, 2) == "00") {
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
                    val result = entry.getName().replace(0.toChar().toString(), "") + "-" + position
                    childrenList.add(result)
                }
                if (block.subSequence(0, 2) == "01") {
                    val entry =
                        FileEntry(
                            binaryFormat,
                            fsConstants,
                            permissionUtils,
                            content,
                        )
                    entry.parseBinary(block)
                    childrenList.add(entry.getName() + "-" + nextPointers.getChildren()[position])
                }
            }
        }
        if (hasMore) {
            loadMoreChildren(childrenList, nextPointers.getNextPointer())
        }
    }

    fun setUsedPosition(
        value: Boolean,
        position: Int,
    ) {
        usedSlots[position] = value
        hardDisk.writeBlock(generateBinary(), currentPosition)
    }

    fun getUsedSlots(): BooleanArray = usedSlots

    fun getChildren(): IntArray = children

    fun setParent(parent: Int) {
        this.parent = parent
    }

    fun generateBinary(): String {
        val binary = StringBuilder()
        binary.append("10")
        val parentBinary = Integer.toBinaryString(parent)
        binary.append(padBinary(parentBinary, fsConstants.POINTER_BITS))
        var usedBits = ""
        var childrenBits = ""
        for (i in 0 until fsConstants.POINTERS_COUNT) {
            if (usedSlots[i]) {
                usedBits += "1"
                childrenBits += padBinary(Integer.toBinaryString(children[i]), fsConstants.POINTER_BITS)
            } else {
                usedBits += "0"
                childrenBits += emptyPointer()
            }
        }
        binary.append(usedBits)
        binary.append(childrenBits)
        if (hasMore) {
            binary.append("1")
            binary.append(padBinary(Integer.toBinaryString(spaceManager.getFreePosition()), fsConstants.POINTER_BITS))
        } else {
            binary.append("0")
            binary.append(emptyPointer())
        }
        return binary.toString()
    }

    private fun emptyPointer(): String = "0000000000000000"

    private fun padBinary(
        s: String,
        size: Int,
    ): String {
        var sLocal = s
        while (sLocal.length < size) {
            sLocal = "0$sLocal"
        }
        return sLocal
    }

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
                    padBinary(Integer.toBinaryString(child), fsConstants.POINTER_BITS),
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
            if (hasMore) {
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
                hasMore = true
                this.nextPointer = newNext.getCurrentPosition()
                newNext.setParent(parent)
                newNext.addChild(child)
            }
        }
        return result
    }

    fun getCurrentPosition(): Int = currentPosition
}
