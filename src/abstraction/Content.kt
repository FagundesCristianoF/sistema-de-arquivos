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
        val binary = StringBuilder()
        binary.append("11")
        var textBinary = ""
        if (content.length < fsConstants.CONTENT_CHUNK_CHARS) {
            for (i in content.indices) {
                textBinary += Integer.toBinaryString(fsConstants.BYTE_PREFIX or content[i].code).substring(1)
            }
            binary.append(binaryFormat.padBinary(textBinary, fsConstants.CONTENT_DATA_BITS))
            binary.append("0")
            binary.append("0000000000000")
            while (binary.toString().length < fsConstants.CONTENT_BLOCK_BITS) {
                binary.append("0")
            }
            hardDisk.writeBlock(binary.toString(), position)
        } else {
            for (i in 0 until fsConstants.CONTENT_CHUNK_CHARS) {
                textBinary += Integer.toBinaryString(fsConstants.BYTE_PREFIX or content[i].code).substring(1)
            }
            binary.append(textBinary)
            binary.append("1")
            val nextContent = generateBinary(content.substring(fsConstants.CONTENT_CHUNK_CHARS))
            binary.append(binaryFormat.padBinary(Integer.toBinaryString(nextContent), fsConstants.POINTER_BITS))
            binary.append("00000")
            hardDisk.writeBlock(binary.toString(), position)
        }
        return position
    }

    fun parseBinary(binary: String): String {
        var result = ""
        var i = fsConstants.CONTENT_HEADER_BITS
        while (i < fsConstants.CONTENT_DATA_BITS) {
            result += (Integer.parseInt(binary.substring(i, i + 8), 2)).toChar()
            i += 8
        }
        if (binary[fsConstants.CONTENT_CONTINUE_BIT_INDEX] == '1') {
            val nextContent =
                Integer.parseInt(
                    binary.substring(fsConstants.CONTENT_NEXT_PTR_START, fsConstants.CONTENT_NEXT_PTR_END),
                    2,
                )
            result += parseBinary(hardDisk.readBlock(nextContent))
        }
        return result.replace(0.toChar().toString(), "")
    }
}
