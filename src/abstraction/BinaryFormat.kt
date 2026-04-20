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
    ): String = value.padStart(size, '0')

    override fun encodeName(
        name: String,
        maxChars: Int,
        targetBits: Int,
    ): String {
        val limit = minOf(name.length, maxChars)
        return buildString {
            for (i in 0 until limit) {
                append((fsConstants.BYTE_PREFIX or name[i].code).toString(2).substring(1))
            }
        }.padEnd(targetBits, '0')
    }

    override fun encodePermissionBits(permission: String): String =
        buildString {
            val end = fsConstants.PERMISSION_STRING_START + fsConstants.PERMISSION_STRING_LENGTH
            for (i in fsConstants.PERMISSION_STRING_START until end) {
                append(if (permission[i] == '-') '0' else '1')
            }
        }

    override fun applyPermissionBits(
        current: String,
        bits: String,
    ): String {
        val permChars = "rxwrxwrxw"
        return current + bits.mapIndexed { i, bit -> if (bit == '1') permChars[i] else '-' }.joinToString("")
    }

    override fun encodeDateBits(date: String): String {
        val day = date.substring(fsConstants.DATE_STRING_DAY_START, fsConstants.DATE_STRING_DAY_END).toInt()
        val month = date.substring(fsConstants.DATE_STRING_MONTH_START, fsConstants.DATE_STRING_MONTH_END).toInt()
        val year =
            date.substring(fsConstants.DATE_STRING_YEAR_START, fsConstants.DATE_STRING_YEAR_END).toInt() -
                fsConstants.YEAR_OFFSET
        val hour = date.substring(fsConstants.DATE_STRING_HOUR_START, fsConstants.DATE_STRING_HOUR_END).toInt()
        val minute = date.substring(fsConstants.DATE_STRING_MINUTE_START, fsConstants.DATE_STRING_MINUTE_END).toInt()
        val second = date.substring(fsConstants.DATE_STRING_SECOND_START, fsConstants.DATE_STRING_SECOND_END).toInt()
        return padBinary(day.toString(2), 5) +
            padBinary(month.toString(2), 4) +
            padBinary(year.toString(2), 3) +
            padBinary(hour.toString(2), 5) +
            padBinary(minute.toString(2), 6) +
            padBinary(second.toString(2), 6)
    }
}
