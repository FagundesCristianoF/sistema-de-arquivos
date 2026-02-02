package abstraction

interface BinaryFormat {
    fun padBinary(
        value: String,
        size: Int,
    ): String

    fun encodeName(
        name: String,
        maxChars: Int,
        targetBits: Int,
    ): String

    fun encodePermissionBits(permission: String): String

    fun applyPermissionBits(
        current: String,
        bits: String,
    ): String

    fun encodeDateBits(date: String): String
}

class DefaultBinaryFormat(
    private val fsConstants: FsConstants,
) : BinaryFormat {
    override fun padBinary(
        value: String,
        size: Int,
    ): String {
        var result = value
        while (result.length < size) {
            result = "0$result"
        }
        return result
    }

    override fun encodeName(
        name: String,
        maxChars: Int,
        targetBits: Int,
    ): String {
        var binaryName = ""
        val limit = minOf(name.length, maxChars)
        for (i in 0 until limit) {
            binaryName += Integer.toBinaryString(fsConstants.BYTE_PREFIX or name[i].code).substring(1)
        }
        while (binaryName.length < targetBits) {
            binaryName += "0"
        }
        return binaryName
    }

    override fun encodePermissionBits(permission: String): String {
        var bits = ""
        for (i in fsConstants.PERMISSION_STRING_START until fsConstants.PERMISSION_STRING_START + fsConstants.PERMISSION_STRING_LENGTH) {
            bits += if (permission[i] == '-') "0" else "1"
        }
        return bits
    }

    override fun applyPermissionBits(
        current: String,
        bits: String,
    ): String {
        var result = current
        result += if (bits[0] == '1') "r" else "-"
        result += if (bits[1] == '1') "x" else "-"
        result += if (bits[2] == '1') "w" else "-"
        result += if (bits[3] == '1') "r" else "-"
        result += if (bits[4] == '1') "x" else "-"
        result += if (bits[5] == '1') "w" else "-"
        result += if (bits[6] == '1') "r" else "-"
        result += if (bits[7] == '1') "x" else "-"
        result += if (bits[8] == '1') "w" else "-"
        return result
    }

    override fun encodeDateBits(date: String): String {
        val day =
            Integer.toBinaryString(
                date.substring(fsConstants.DATE_STRING_DAY_START, fsConstants.DATE_STRING_DAY_END).toInt(),
            )
        val month =
            Integer.toBinaryString(
                date.substring(fsConstants.DATE_STRING_MONTH_START, fsConstants.DATE_STRING_MONTH_END).toInt(),
            )
        val year =
            Integer.toBinaryString(
                date.substring(fsConstants.DATE_STRING_YEAR_START, fsConstants.DATE_STRING_YEAR_END).toInt() -
                    fsConstants.YEAR_OFFSET,
            )
        val hour =
            Integer.toBinaryString(
                date.substring(fsConstants.DATE_STRING_HOUR_START, fsConstants.DATE_STRING_HOUR_END).toInt(),
            )
        val minute =
            Integer.toBinaryString(
                date.substring(fsConstants.DATE_STRING_MINUTE_START, fsConstants.DATE_STRING_MINUTE_END).toInt(),
            )
        val second =
            Integer.toBinaryString(
                date.substring(fsConstants.DATE_STRING_SECOND_START, fsConstants.DATE_STRING_SECOND_END).toInt(),
            )
        return padBinary(day, 5) +
            padBinary(month, 4) +
            padBinary(year, 3) +
            padBinary(hour, 5) +
            padBinary(minute, 6) +
            padBinary(second, 6)
    }
}
