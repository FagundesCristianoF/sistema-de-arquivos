package binary

import java.util.Arrays

private const val HEX_PREFIX = "0x"
private const val HEX_PREFIX_LEN = 2
private const val HEX_INT_LENGTH = 10
private const val HEX_LONG_LENGTH = 18
private val chars = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')
private const val UNSIGNED_BASE: Long = 0x7FFFFFFFL + 0x7FFFFFFFL + 2L

fun intToBinaryString(
    value: Int,
    length: Int,
): String {
    val result = CharArray(length)
    var index = length - 1
    for (i in 0 until length) {
        result[index] = if (bitValue(value, i) == 1) '1' else '0'
        index--
    }
    return String(result)
}

fun intToBinaryString(value: Int): String = intToBinaryString(value, 32)

fun longToBinaryString(
    value: Long,
    length: Int,
): String {
    val result = CharArray(length)
    var index = length - 1
    for (i in 0 until length) {
        result[index] = if (bitValue(value, i) == 1) '1' else '0'
        index--
    }
    return String(result)
}

fun longToBinaryString(value: Long): String = longToBinaryString(value, 64)

fun binaryStringToInt(value: String): Int {
    var result = value[0].code - 48
    for (i in 1 until value.length) {
        result = (result shl 1) or (value[i].code - 48)
    }
    return result
}

fun binaryStringToLong(value: String): Long {
    var result = (value[0].code - 48).toLong()
    for (i in 1 until value.length) {
        result = (result shl 1) or (value[i].code - 48).toLong()
    }
    return result
}

fun binaryStringToHexString(value: String): String {
    val digits = (value.length + 3) / 4
    val hexChars = CharArray(digits + 2)
    var position = value.length - 1
    hexChars[0] = '0'
    hexChars[1] = 'x'
    for (digs in 0 until digits) {
        var result = 0
        var pow = 1
        var rep = 0
        while (rep < 4 && position >= 0) {
            if (value[position] == '1') result += pow
            pow *= 2
            position--
            rep++
        }
        hexChars[digits - digs + 1] = chars[result]
    }
    return String(hexChars)
}

fun hexStringToBinaryString(value: String): String {
    var result = ""
    var working = value
    if (working.indexOf("0x") == 0 || working.indexOf("0X") == 0) working = working.substring(2)
    for (digs in 0 until working.length) {
        result +=
            when (working[digs]) {
                '0' -> "0000"
                '1' -> "0001"
                '2' -> "0010"
                '3' -> "0011"
                '4' -> "0100"
                '5' -> "0101"
                '6' -> "0110"
                '7' -> "0111"
                '8' -> "1000"
                '9' -> "1001"
                'a', 'A' -> "1010"
                'b', 'B' -> "1011"
                'c', 'C' -> "1100"
                'd', 'D' -> "1101"
                'e', 'E' -> "1110"
                'f', 'F' -> "1111"
                else -> ""
            }
    }
    return result
}

fun binaryStringToHexDigit(value: String): Char {
    if (value.length > 4) return '0'
    var result = 0
    var pow = 1
    for (i in value.length - 1 downTo 0) {
        if (value[i] == '1') result += pow
        pow *= 2
    }
    return chars[result]
}

fun intToHexString(d: Int): String {
    var t = Integer.toHexString(d)
    while (t.length < 8) t = "0$t"
    return "0x$t"
}

fun longToHexString(value: Long): String = binaryStringToHexString(longToBinaryString(value))

fun unsignedIntToIntString(d: Int): String = if (d >= 0) Integer.toString(d) else java.lang.Long.toString(UNSIGNED_BASE + d)

@Throws(NumberFormatException::class)
fun stringToInt(s: String): Int {
    var work = s
    return try {
        Integer.decode(s)
    } catch (nfe: NumberFormatException) {
        work = work.lowercase()
        if (work.length == HEX_INT_LENGTH && work.startsWith(HEX_PREFIX)) {
            var bitString = ""
            for (i in HEX_PREFIX_LEN until HEX_INT_LENGTH) {
                val index = Arrays.binarySearch(chars, work[i])
                if (index < 0) throw NumberFormatException()
                bitString += intToBinaryString(index, 4)
            }
            binaryStringToInt(bitString)
        } else {
            throw NumberFormatException()
        }
    }
}

@Throws(NumberFormatException::class)
fun stringToLong(s: String): Long {
    var work = s
    return try {
        java.lang.Long.decode(s)
    } catch (nfe: NumberFormatException) {
        work = work.lowercase()
        if (work.length == HEX_LONG_LENGTH && work.startsWith(HEX_PREFIX)) {
            var bitString = ""
            for (i in HEX_PREFIX_LEN until HEX_LONG_LENGTH) {
                val index = Arrays.binarySearch(chars, work[i])
                if (index < 0) throw NumberFormatException()
                bitString += intToBinaryString(index, 4)
            }
            binaryStringToLong(bitString)
        } else {
            throw NumberFormatException()
        }
    }
}

fun highOrderLongToInt(longValue: Long): Int = (longValue shr 32).toInt()

fun lowOrderLongToInt(longValue: Long): Int = (longValue shl 32 shr 32).toInt()

fun twoIntsToLong(
    highOrder: Int,
    lowOrder: Int,
): Long = (highOrder.toLong() shl 32) or (lowOrder.toLong() and 0xFFFFFFFFL)

fun bitValue(
    value: Int,
    bit: Int,
): Int = 1 and (value shr bit)

fun bitValue(
    value: Long,
    bit: Int,
): Int = (1L and (value shr bit)).toInt()

fun setBit(
    value: Int,
    bit: Int,
): Int = value or (1 shl bit)

fun clearBit(
    value: Int,
    bit: Int,
): Int = value and (1 shl bit).inv()

fun setByte(
    value: Int,
    bite: Int,
    replace: Int,
): Int = value and (0xFF shl (bite shl 3)).inv() or ((replace and 0xFF) shl (bite shl 3))

fun getByte(
    value: Int,
    bite: Int,
): Int = value shl ((3 - bite) shl 3) ushr 24

fun isHex(v: String): Boolean {
    try {
        try {
            stringToInt(v)
        } catch (nfe: NumberFormatException) {
            try {
                stringToLong(v)
            } catch (e: NumberFormatException) {
                return false
            }
        }
        if ((v[0] == '-') && (v[1] == '0') && (v[1].uppercaseChar() == 'X')) {
            return true
        } else if ((v[0] == '0') && (v[1].uppercaseChar() == 'X')) {
            return true
        }
    } catch (e: StringIndexOutOfBoundsException) {
        return false
    }
    return false
}

fun isOctal(v: String): Boolean {
    try {
        stringToInt(v)
        if (isHex(v)) return false
        if ((v[0] == '-') && (v[1] == '0') && (v.length > 1)) {
            return true
        } else if ((v[0] == '0') && (v.length > 1)) {
            return true
        }
    } catch (e: StringIndexOutOfBoundsException) {
        return false
    } catch (e: NumberFormatException) {
        return false
    }
    return false
}
