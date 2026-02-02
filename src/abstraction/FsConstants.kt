@file:Suppress("PropertyName")

package abstraction

interface FsConstants {
    val DISK_TOTAL_BITS: Int
    val DATA_REGION_START: Int
    val BITMAP_BITS: Int
    val BLOCK_SIZE_BITS: Int

    val POINTER_BITS: Int
    val POINTERS_COUNT: Int

    val NAME_BITS_START: Int
    val NAME_BITS_END: Int
    val NAME_MAX_CHARS: Int

    val DATE_DAY_START: Int
    val DATE_DAY_END: Int
    val DATE_MONTH_END: Int
    val DATE_YEAR_END: Int
    val DATE_HOUR_END: Int
    val DATE_MINUTE_END: Int
    val DATE_SECOND_END: Int

    val DATE_STRING_DAY_START: Int
    val DATE_STRING_DAY_END: Int
    val DATE_STRING_MONTH_START: Int
    val DATE_STRING_MONTH_END: Int
    val DATE_STRING_YEAR_START: Int
    val DATE_STRING_YEAR_END: Int
    val DATE_STRING_HOUR_START: Int
    val DATE_STRING_HOUR_END: Int
    val DATE_STRING_MINUTE_START: Int
    val DATE_STRING_MINUTE_END: Int
    val DATE_STRING_SECOND_START: Int
    val DATE_STRING_SECOND_END: Int

    val BASE_YEAR: Int
    val YEAR_OFFSET: Int

    val BYTE_PREFIX: Int

    val PERMISSION_STRING_START: Int
    val PERMISSION_STRING_LENGTH: Int
    val CHMOD_GROUPS: Int

    val PERMISSION_BITS_START: Int
    val PERMISSION_BITS_END: Int
    val PERMISSION_BITS_LENGTH: Int

    val PARENT_POINTER_START: Int
    val PARENT_POINTER_END: Int
    val CHILD_POINTER_START: Int
    val CHILD_POINTER_END: Int

    val CONTENT_HEADER_BITS: Int
    val CONTENT_DATA_BITS: Int
    val CONTENT_BLOCK_BITS: Int
    val CONTENT_CHUNK_CHARS: Int
    val CONTENT_CONTINUE_BIT_INDEX: Int
    val CONTENT_NEXT_PTR_START: Int
    val CONTENT_NEXT_PTR_END: Int

    val POINTER_PARENT_START: Int
    val POINTER_PARENT_END: Int
    val POINTER_USED_START: Int
    val POINTER_USED_COUNT: Int
    val POINTER_CHILDREN_START: Int
}

class DefaultFsConstants : FsConstants {
    override val DISK_TOTAL_BITS: Int = 16 * 8 * 1024 * 1024
    override val DATA_REGION_START: Int = 65536
    override val BITMAP_BITS: Int = DATA_REGION_START
    override val BLOCK_SIZE_BITS: Int = 256

    override val POINTER_BITS: Int = 16
    override val POINTERS_COUNT: Int = 13

    override val NAME_BITS_START: Int = 2
    override val NAME_BITS_END: Int = 184
    override val NAME_MAX_CHARS: Int = 23

    override val DATE_DAY_START: Int = 186
    override val DATE_DAY_END: Int = 191
    override val DATE_MONTH_END: Int = 195
    override val DATE_YEAR_END: Int = 198
    override val DATE_HOUR_END: Int = 203
    override val DATE_MINUTE_END: Int = 209
    override val DATE_SECOND_END: Int = 215

    override val DATE_STRING_DAY_START: Int = 0
    override val DATE_STRING_DAY_END: Int = 2
    override val DATE_STRING_MONTH_START: Int = 3
    override val DATE_STRING_MONTH_END: Int = 5
    override val DATE_STRING_YEAR_START: Int = 6
    override val DATE_STRING_YEAR_END: Int = 8
    override val DATE_STRING_HOUR_START: Int = 9
    override val DATE_STRING_HOUR_END: Int = 11
    override val DATE_STRING_MINUTE_START: Int = 12
    override val DATE_STRING_MINUTE_END: Int = 14
    override val DATE_STRING_SECOND_START: Int = 15
    override val DATE_STRING_SECOND_END: Int = 17

    override val BASE_YEAR: Int = 2017
    override val YEAR_OFFSET: Int = 17

    override val BYTE_PREFIX: Int = 0x100

    override val PERMISSION_STRING_START: Int = 1
    override val PERMISSION_STRING_LENGTH: Int = 9
    override val CHMOD_GROUPS: Int = 3

    override val PERMISSION_BITS_START: Int = 215
    override val PERMISSION_BITS_END: Int = 224
    override val PERMISSION_BITS_LENGTH: Int = 9

    override val PARENT_POINTER_START: Int = 224
    override val PARENT_POINTER_END: Int = 240
    override val CHILD_POINTER_START: Int = 240
    override val CHILD_POINTER_END: Int = 256

    override val CONTENT_HEADER_BITS: Int = 2
    override val CONTENT_DATA_BITS: Int = 232
    override val CONTENT_BLOCK_BITS: Int = 256
    override val CONTENT_CHUNK_CHARS: Int = 29
    override val CONTENT_CONTINUE_BIT_INDEX: Int = 234
    override val CONTENT_NEXT_PTR_START: Int = 235
    override val CONTENT_NEXT_PTR_END: Int = 251

    override val POINTER_PARENT_START: Int = 2
    override val POINTER_PARENT_END: Int = 18
    override val POINTER_USED_START: Int = 18
    override val POINTER_USED_COUNT: Int = 13
    override val POINTER_CHILDREN_START: Int = 31
}
