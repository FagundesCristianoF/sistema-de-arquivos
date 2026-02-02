package binary

import java.util.Arrays

/**
 * Utility class for binary conversions.
 * @author douglas
 */
class Binary {
    companion object {
        private const val HEX_PREFIX = "0x"
        private const val HEX_PREFIX_LEN = 2
        private const val HEX_INT_LENGTH = 10
        private const val HEX_LONG_LENGTH = 18

        // Using int value 0-15 as index, yields equivalent hex digit as char.
        private val chars = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

        // Use this to produce String equivalent of unsigned int value (add it to int value, result is long)
        private const val UNSIGNED_BASE: Long = 0x7FFFFFFFL + 0x7FFFFFFFL + 2L // 0xFFFFFFFF+1

        /**
         * Convert an integer value into a binary string.
         *
         * @param value The integer value to convert.
         * @param length Number of positions for the binary string.
         * @return String containing 0's and 1's equivalent to the integer value.
         */
        @JvmStatic
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

        /**
         * Translate int value into a String consisting of '1's and '0's.  Assumes all 32 bits are
         * to be translated.
         *
         * @param value The int value to convert.
         * @return String consisting of '1' and '0' characters corresponding to the requested binary sequence.
         */
        @JvmStatic
        fun intToBinaryString(value: Int): String = intToBinaryString(value, 32)

        /**
         * Translate long value into a String consisting of '1's and '0's.
         *
         * @param value The long value to convert.
         * @param length The number of bit positions, starting at least significant, to process.
         * @return String consisting of '1' and '0' characters corresponding to the requested binary sequence.
         */
        @JvmStatic
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

        /**
         * Translate long value into a String consisting of '1's and '0's.  Assumes all 64 bits are
         * to be translated.
         *
         * @param value The long value to convert.
         * @return String consisting of '1' and '0' characters corresponding to the requested binary sequence.
         */
        @JvmStatic
        fun longToBinaryString(value: Long): String = longToBinaryString(value, 64)

        /**
         * Translate String consisting of '1's and '0's into an int value having that binary representation.
         * The String is assumed to be at most 32 characters long.  No error checking is performed.
         * String position 0 has most-significant bit, position length-1 has least-significant.
         *
         * @param value The String value to convert.
         * @return int whose binary value corresponds to decoded String.
         */
        @JvmStatic
        fun binaryStringToInt(value: String): Int {
            var result = value[0].code - 48
            for (i in 1 until value.length) {
                result = (result shl 1) or (value[i].code - 48)
            }
            return result
        }

        /**
         * Translate String consisting of '1's and '0's into a long value having that binary representation.
         * The String is assumed to be at most 64 characters long.  No error checking is performed.
         * String position 0 has most-significant bit, position length-1 has least-significant.
         *
         * @param value The String value to convert.
         * @return long whose binary value corresponds to decoded String.
         */
        @JvmStatic
        fun binaryStringToLong(value: String): Long {
            var result = (value[0].code - 48).toLong()
            for (i in 1 until value.length) {
                result = (result shl 1) or (value[i].code - 48).toLong()
            }
            return result
        }

        /**
         * Translate String consisting of '1's and '0's into String equivalent of the corresponding
         * hexadecimal value.  No length limit.
         * String position 0 has most-significant bit, position length-1 has least-significant.
         *
         * @param value The String value to convert.
         * @return String containing '0', '1', ...'F' characters which form hexadecimal
         * equivalent of decoded String.
         */
        @JvmStatic
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
                    if (value[position] == '1') {
                        result += pow
                    }
                    pow *= 2
                    position--
                    rep++
                }
                hexChars[digits - digs + 1] = chars[result]
            }
            return String(hexChars)
        }

        /**
         * Translate String consisting of hexadecimal digits into String consisting of
         * corresponding binary digits ('1's and '0's).  No length limit.
         * String position 0 will have most-significant bit, position length-1 has least-significant.
         *
         * @param value String containing '0', '1', ...'f'
         * characters which form hexadecimal.  Letters may be either upper or lower case.
         * Works either with or without leading "Ox".
         * @return String with equivalent value in binary.
         */
        @JvmStatic
        fun hexStringToBinaryString(value: String): String {
            var result = ""
            var working = value
            // slice off leading Ox or 0X
            if (working.indexOf("0x") == 0 || working.indexOf("0X") == 0) {
                working = working.substring(2)
            }
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

        /**
         * Translate String consisting of '1's and '0's into char equivalent of the corresponding
         * hexadecimal digit.  String limited to length 4.
         * String position 0 has most-significant bit, position length-1 has least-significant.
         *
         * @param value The String value to convert.
         * @return char '0', '1', ...'F' which form hexadecimal equivalent of decoded String.
         * If string length > 4, returns '0'.
         */
        @JvmStatic
        fun binaryStringToHexDigit(value: String): Char {
            if (value.length > 4) {
                return '0'
            }
            var result = 0
            var pow = 1
            for (i in value.length - 1 downTo 0) {
                if (value[i] == '1') {
                    result += pow
                }
                pow *= 2
            }
            return chars[result]
        }

        /**
         * Prefix a hexadecimal-indicating string "0x" to the string which is
         * returned by the method "Integer.toHexString". Prepend leading zeroes
         * to that string as necessary to make it always eight hexadecimal digits.
         *
         * @param d The int value to convert.
         * @return String containing '0', '1', ...'F' which form hexadecimal equivalent of int.
         */
        @JvmStatic
        fun intToHexString(d: Int): String {
            val leadingZero = "0"
            val leadingX = "0x"
            var t = Integer.toHexString(d)
            while (t.length < 8) {
                t = leadingZero + t
            }
            t = leadingX + t
            return t
        }

        /**
         * Prefix a hexadecimal-indicating string "0x" to the string equivalent to the
         * hexadecimal value in the long parameter. Prepend leading zeroes
         * to that string as necessary to make it always sixteen hexadecimal digits.
         *
         * @param value The long value to convert.
         * @return String containing '0', '1', ...'F' which form hexadecimal equivalent of long.
         */
        @JvmStatic
        fun longToHexString(value: Long): String = binaryStringToHexString(longToBinaryString(value))

        /**
         * Produce String equivalent of integer value interpreting it as an unsigned integer.
         * For instance,  -1 (0xffffffff) produces "4294967295" instead of "-1".
         * @param d The int value to interpret.
         * @return String which forms unsigned 32 bit equivalent of int.
         */
        @JvmStatic
        fun unsignedIntToIntString(d: Int): String = if (d >= 0) Integer.toString(d) else java.lang.Long.toString(UNSIGNED_BASE + d)

        /**
         * Attempt to validate given string whose characters represent a 32 bit integer.
         * Integer.decode() is insufficient because it will not allow incorporation of
         * hex two's complement (i.e. 0x80...0 through 0xff...f).  Allows
         * optional negative (-) sign but no embedded spaces.
         *
         * @param s candidate string
         * @return returns int value represented by given string
         * @throws NumberFormatException if string cannot be translated into an int
         */
        @JvmStatic
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
                        if (index < 0) {
                            throw NumberFormatException()
                        }
                        bitString += intToBinaryString(index, 4)
                    }
                    binaryStringToInt(bitString)
                } else {
                    throw NumberFormatException()
                }
            }
        }

        /**
         * Attempt to validate given string whose characters represent a 64 bit long.
         * Long.decode() is insufficient because it will not allow incorporation of
         * hex two's complement (i.e. 0x80...0 through 0xff...f).  Allows
         * optional negative (-) sign but no embedded spaces.
         *
         * @param s candidate string
         * @return returns long value represented by given string
         * @throws NumberFormatException if string cannot be translated into a long
         */
        @JvmStatic
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
                        if (index < 0) {
                            throw NumberFormatException()
                        }
                        bitString += intToBinaryString(index, 4)
                    }
                    binaryStringToLong(bitString)
                } else {
                    throw NumberFormatException()
                }
            }
        }

        /**
         *  Returns int representing the bit values of the high order 32 bits of given
         *  64 bit long value.
         *   @param longValue The long value from which to extract bits.
         *   @return int containing high order 32 bits of argument
         **/
        @JvmStatic
        fun highOrderLongToInt(longValue: Long): Int = (longValue shr 32).toInt()

        /**
         *  Returns int representing the bit values of the low order 32 bits of given
         *  64 bit long value.
         *   @param longValue The long value from which to extract bits.
         *   @return int containing low order 32 bits of argument
         **/
        @JvmStatic
        fun lowOrderLongToInt(longValue: Long): Int = (longValue shl 32 shr 32).toInt()

        /**
         *  Returns long (64 bit integer) combining the bit values of two given 32 bit
         *  integer values.
         *   @param highOrder Integer to form the high-order 32 bits of result.
         *   @param lowOrder Integer to form the high-order 32 bits of result.
         *   @return long containing concatenated 32 bit int values.
         **/
        @JvmStatic
        fun twoIntsToLong(
            highOrder: Int,
            lowOrder: Int,
        ): Long = (highOrder.toLong() shl 32) or (lowOrder.toLong() and 0xFFFFFFFFL)

        /**
         *  Returns the bit value of the given bit position of the given int value.
         *   @param value The value to read the bit from.
         *   @param bit bit position in range 0 (least significant) to 31 (most)
         *   @return 0 if the bit position contains 0, and 1 otherwise.
         **/
        @JvmStatic
        fun bitValue(
            value: Int,
            bit: Int,
        ): Int = 1 and (value shr bit)

        /**
         *  Returns the bit value of the given bit position of the given long value.
         *   @param value The value to read the bit from.
         *   @param bit bit position in range 0 (least significant) to 63 (most)
         *   @return 0 if the bit position contains 0, and 1 otherwise.
         **/
        @JvmStatic
        fun bitValue(
            value: Long,
            bit: Int,
        ): Int = (1L and (value shr bit)).toInt()

        /**
         *  Sets the specified bit of the specified value to 1, and returns the result.
         *   @param value The value in which the bit is to be set.
         *   @param bit bit position in range 0 (least significant) to 31 (most)
         *   @return value possibly modified with given bit set to 1.
         **/
        @JvmStatic
        fun setBit(
            value: Int,
            bit: Int,
        ): Int = value or (1 shl bit)

        /**
         *  Sets the specified bit of the specified value to 0, and returns the result.
         *   @param value The value in which the bit is to be set.
         *   @param bit bit position in range 0 (least significant) to 31 (most)
         *   @return value possibly modified with given bit set to 0.
         **/
        @JvmStatic
        fun clearBit(
            value: Int,
            bit: Int,
        ): Int = value and (1 shl bit).inv()

        /**
         *  Sets the specified byte of the specified value to the low order 8 bits of
         *  specified replacement value, and returns the result.
         *   @param value The value in which the byte is to be set.
         *   @param bite byte position in range 0 (least significant) to 3 (most)
         *   @param replace value to place into that byte position - use low order 8 bits
         *   @return value modified value.
         **/
        @JvmStatic
        fun setByte(
            value: Int,
            bite: Int,
            replace: Int,
        ): Int = value and (0xFF shl (bite shl 3)).inv() or ((replace and 0xFF) shl (bite shl 3))

        /**
         *  Gets the specified byte of the specified value.
         *   @param value The value in which the byte is to be retrieved.
         *   @param bite byte position in range 0 (least significant) to 3 (most)
         *   @return zero-extended byte value in low order byte.
         **/
        @JvmStatic
        fun getByte(
            value: Int,
            bite: Int,
        ): Int = value shl ((3 - bite) shl 3) ushr 24

        /**
         * Parsing method to see if a string represents a hex number.
         *  As per http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Integer.html#decode(java.lang.String),
         *  a string represents a hex number if the string is in the forms:
         *      Signopt 0x HexDigits
         *      Signopt 0X HexDigits
         *      Signopt # HexDigits   <---- Disallow this form since # is MIPS comment
         *
         * @param v String containing numeric digits (could be decimal, octal, or hex)
         *
         * @return Returns <tt>true</tt> if string represents a hex number, else returns <tt>false</tt>.
         */
        @JvmStatic
        fun isHex(v: String): Boolean {
            try {
                try {
                    Binary.stringToInt(v)
                } catch (nfe: NumberFormatException) {
                    try {
                        Binary.stringToLong(v)
                    } catch (e: NumberFormatException) {
                        return false
                    }
                }
                if ((v[0] == '-') &&
                    (v[1] == '0') &&
                    (v[1].uppercaseChar() == 'X')
                ) {
                    return true
                } else if ((v[0] == '0') &&
                    (v[1].uppercaseChar() == 'X')
                ) {
                    return true
                }
            } catch (e: StringIndexOutOfBoundsException) {
                return false
            }
            return false
        }

        /**
         * Parsing method to see if a string represents an octal number.
         *  As per http://java.sun.com/j2se/1.4.2/docs/api/java/lang/Integer.html#decode(java.lang.String),
         *  a string represents an octal number if the string is in the forms:
         *      Signopt 0 OctalDigits
         *
         * @param v String containing numeric digits (could be decimal, octal, or hex)
         *
         * @return Returns <tt>true</tt> if string represents an octal number, else returns <tt>false</tt>.
         */
        @JvmStatic
        fun isOctal(v: String): Boolean {
            try {
                Binary.stringToInt(v)
                if (isHex(v)) {
                    return false
                }
                if ((v[0] == '-') &&
                    (v[1] == '0') &&
                    (v.length > 1)
                ) {
                    return true
                } else if ((v[0] == '0') &&
                    (v.length > 1)
                ) {
                    return true
                }
            } catch (e: StringIndexOutOfBoundsException) {
                return false
            } catch (e: NumberFormatException) {
                return false
            }
            return false
        }
    }
}
