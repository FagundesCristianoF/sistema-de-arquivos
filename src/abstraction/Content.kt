package abstraction

import hardware.HardDisk

class Content(
    private val hardDisk: HardDisk,
    private val spaceManager: SpaceManager,
    private val fsConstants: FsConstants,
    private val binaryFormat: BinaryFormat,
) {
    fun generateBinary(content: String): Int {
        val position = spaceManager.getFreePosition()
        val block =
            if (content.length < fsConstants.CONTENT_CHUNK_CHARS) {
                buildString {
                    append("11")
                    val textBinary =
                        buildString {
                            for (c in content) {
                                append((fsConstants.BYTE_PREFIX or c.code).toString(2).substring(1))
                            }
                        }
                    append(binaryFormat.padBinary(textBinary, fsConstants.CONTENT_DATA_BITS))
                    append("0")
                    append("0000000000000")
                }.padEnd(fsConstants.CONTENT_BLOCK_BITS, '0')
            } else {
                buildString {
                    append("11")
                    for (i in 0 until fsConstants.CONTENT_CHUNK_CHARS) {
                        append((fsConstants.BYTE_PREFIX or content[i].code).toString(2).substring(1))
                    }
                    append("1")
                    val nextContent = generateBinary(content.substring(fsConstants.CONTENT_CHUNK_CHARS))
                    append(binaryFormat.padBinary(nextContent.toString(2), fsConstants.POINTER_BITS))
                    append("00000")
                }
            }
        hardDisk.writeBlock(block, position)
        return position
    }

    fun parseBinary(binary: String): String {
        val result =
            buildString {
                var i = fsConstants.CONTENT_HEADER_BITS
                while (i < fsConstants.CONTENT_DATA_BITS) {
                    append(binary.substring(i, i + 8).toInt(2).toChar())
                    i += 8
                }
            }
        return if (binary[fsConstants.CONTENT_CONTINUE_BIT_INDEX] == '1') {
            val nextContent = binary.substring(fsConstants.CONTENT_NEXT_PTR_START, fsConstants.CONTENT_NEXT_PTR_END).toInt(2)
            (result + parseBinary(hardDisk.readBlock(nextContent))).replace(0.toChar().toString(), "")
        } else {
            result.replace(0.toChar().toString(), "")
        }
    }
}
