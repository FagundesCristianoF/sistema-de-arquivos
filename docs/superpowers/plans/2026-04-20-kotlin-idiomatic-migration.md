# Idiomatic Kotlin Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace Java-influenced patterns throughout the codebase with idiomatic Kotlin, keeping all logic identical and passing detekt + ktlint + tests at every step.

**Architecture:** Eight independent refactor tasks applied file-by-file; each ends with a full test + lint run and a commit. No logic changes, no new features — only idiom improvements.

**Tech Stack:** Kotlin 2.3, Gradle with detekt 1.23.8 + ktlint 14.0.1, JUnit via `kotlin("test")`, MockK.

---

## File Map

| File | Change |
|------|--------|
| `src/operatingSystem/Kernel.kt` | Remove all KDoc blocks |
| `src/binary/Binary.kt` | Remove `class`/`companion object` wrapper → top-level functions |
| `src/di/AppModule.kt` | Remove `singleOf(::Binary)` |
| `src/operations/KernelContext.kt` | Remove `val binary: Binary`; change `ArrayList<String>` → `List<String>` in `fileDoesNotExist` |
| `src/abstraction/BinaryFormat.kt` | `padBinary` → `padStart`; `encodeName`/`encodePermissionBits`/`applyPermissionBits` → idiomatic |
| `src/abstraction/BinaryDateExtensions.kt` | `Integer.parseInt(..., 2)` → `.toInt(2)` |
| `src/abstraction/Content.kt` | `Integer.parseInt` → `.toInt(2)`, `Integer.toBinaryString` → `.toString(2)`, `StringBuilder` → `buildString` |
| `src/abstraction/FileEntry.kt` | Remove getters/setters → `var` properties; `buildString`; `.toInt(2)` / `.toString(2)` |
| `src/abstraction/CurrentDirectory.kt` | Same as FileEntry |
| `src/abstraction/Pointers.kt` | `toInt(2)`/`toString(2)`; `mutableListOf`; `buildString`; extract `entryName()` helper |
| `src/operations/KernelContext.kt` | `buildString` in `listDirectoryDetailed`/`dumpDirectory`; `none {}` in `fileDoesNotExist` |
| `src/operatingSystem/fileSystem/FileSystemSimulator.kt` | `dispatchCommand` if/else → `when`; `ArrayList` → `mutableListOf` |
| `src/operations/ChmodOperation.kt` | Permission `StringBuilder` → `buildString` |
| `test/operations/OperationsTest.kt` | Update call sites after property migration |
| All operation files that call FileEntry/CurrentDirectory methods | Update getter/setter calls to property access |

---

## Task 1: Remove KDoc from Kernel.kt

**Files:**
- Modify: `src/operatingSystem/Kernel.kt`

- [ ] **Step 1: Confirm tests are green**

```bash
./gradlew test
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Replace the entire file**

```kotlin
package operatingSystem

interface Kernel {
    fun ls(parameters: String): String
    fun mkdir(parameters: String): String
    fun cd(parameters: String): String
    fun rmdir(parameters: String): String
    fun cp(parameters: String): String
    fun mv(parameters: String): String
    fun rm(parameters: String): String
    fun chmod(parameters: String): String
    fun createfile(parameters: String): String
    fun cat(parameters: String): String
    fun batch(parameters: String): String
    fun dump(parameters: String): String
    fun info(): String
}
```

- [ ] **Step 3: Run checks**

```bash
./gradlew test detekt ktlintCheck
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add src/operatingSystem/Kernel.kt
git commit -m "refactor: remove redundant KDoc from Kernel interface"
```

---

## Task 2: Binary.kt → top-level functions

**Files:**
- Modify: `src/binary/Binary.kt`
- Modify: `src/di/AppModule.kt`
- Modify: `src/operations/KernelContext.kt`

- [ ] **Step 1: Replace Binary.kt** — remove the `class Binary` and `companion object` wrappers; make all members top-level; self-calls `Binary.stringToInt` / `Binary.stringToLong` inside `isHex`/`isOctal` become plain `stringToInt` / `stringToLong`:

```kotlin
package binary

import java.util.Arrays

private const val HEX_PREFIX = "0x"
private const val HEX_PREFIX_LEN = 2
private const val HEX_INT_LENGTH = 10
private const val HEX_LONG_LENGTH = 18
private val chars = charArrayOf('0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f')
private const val UNSIGNED_BASE: Long = 0x7FFFFFFFL + 0x7FFFFFFFL + 2L

fun intToBinaryString(value: Int, length: Int): String {
    val result = CharArray(length)
    var index = length - 1
    for (i in 0 until length) {
        result[index] = if (bitValue(value, i) == 1) '1' else '0'
        index--
    }
    return String(result)
}

fun intToBinaryString(value: Int): String = intToBinaryString(value, 32)

fun longToBinaryString(value: Long, length: Int): String {
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
        result += when (working[digs]) {
            '0' -> "0000"; '1' -> "0001"; '2' -> "0010"; '3' -> "0011"
            '4' -> "0100"; '5' -> "0101"; '6' -> "0110"; '7' -> "0111"
            '8' -> "1000"; '9' -> "1001"
            'a', 'A' -> "1010"; 'b', 'B' -> "1011"
            'c', 'C' -> "1100"; 'd', 'D' -> "1101"
            'e', 'E' -> "1110"; 'f', 'F' -> "1111"
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

fun unsignedIntToIntString(d: Int): String =
    if (d >= 0) Integer.toString(d) else java.lang.Long.toString(UNSIGNED_BASE + d)

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

fun twoIntsToLong(highOrder: Int, lowOrder: Int): Long =
    (highOrder.toLong() shl 32) or (lowOrder.toLong() and 0xFFFFFFFFL)

fun bitValue(value: Int, bit: Int): Int = 1 and (value shr bit)

fun bitValue(value: Long, bit: Int): Int = (1L and (value shr bit)).toInt()

fun setBit(value: Int, bit: Int): Int = value or (1 shl bit)

fun clearBit(value: Int, bit: Int): Int = value and (1 shl bit).inv()

fun setByte(value: Int, bite: Int, replace: Int): Int =
    value and (0xFF shl (bite shl 3)).inv() or ((replace and 0xFF) shl (bite shl 3))

fun getByte(value: Int, bite: Int): Int = value shl ((3 - bite) shl 3) ushr 24

fun isHex(v: String): Boolean {
    try {
        try { stringToInt(v) } catch (nfe: NumberFormatException) {
            try { stringToLong(v) } catch (e: NumberFormatException) { return false }
        }
        if ((v[0] == '-') && (v[1] == '0') && (v[1].uppercaseChar() == 'X')) return true
        else if ((v[0] == '0') && (v[1].uppercaseChar() == 'X')) return true
    } catch (e: StringIndexOutOfBoundsException) { return false }
    return false
}

fun isOctal(v: String): Boolean {
    try {
        stringToInt(v)
        if (isHex(v)) return false
        if ((v[0] == '-') && (v[1] == '0') && (v.length > 1)) return true
        else if ((v[0] == '0') && (v.length > 1)) return true
    } catch (e: StringIndexOutOfBoundsException) { return false }
      catch (e: NumberFormatException) { return false }
    return false
}
```

- [ ] **Step 2: Remove `binary: Binary` from KernelContext interface**

In `src/operations/KernelContext.kt`, remove the import and the field:
- Remove `import binary.Binary`
- Remove `val binary: Binary` from the `KernelContext` interface
- Remove `override val binary: Binary` and its constructor parameter from `DefaultKernelContext`

The `DefaultKernelContext` primary constructor becomes:
```kotlin
class DefaultKernelContext(
    override val logger: Logger,
    override val hardDisk: HardDisk,
    override val spaceManager: SpaceManager,
    override val fsConstants: FsConstants,
    override val binaryFormat: BinaryFormat,
    override val permissionUtils: PermissionUtils,
    override val content: Content,
) : KernelContext {
```

- [ ] **Step 3: Remove Binary from AppModule**

In `src/di/AppModule.kt`:
- Remove `import binary.Binary`
- Remove `singleOf(::Binary)` line

- [ ] **Step 4: Run checks**

```bash
./gradlew test detekt ktlintCheck
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add src/binary/Binary.kt src/di/AppModule.kt src/operations/KernelContext.kt
git commit -m "refactor: convert Binary class to top-level functions, remove unused DI binding"
```

---

## Task 3: Standard library replacements

**Files:** `src/abstraction/BinaryDateExtensions.kt`, `src/abstraction/BinaryFormat.kt`, `src/abstraction/Content.kt`, `src/abstraction/Pointers.kt`

- [ ] **Step 1: Replace BinaryDateExtensions.kt** — all `Integer.parseInt` → `.toInt(2)`:

```kotlin
package abstraction

fun String.parseDay(fsConstants: FsConstants): Int =
    substring(fsConstants.DATE_DAY_START, fsConstants.DATE_DAY_END).toInt(2)

fun String.parseMonth(fsConstants: FsConstants): Int =
    substring(fsConstants.DATE_DAY_END, fsConstants.DATE_MONTH_END).toInt(2)

fun String.parseYear(fsConstants: FsConstants): Int =
    substring(fsConstants.DATE_MONTH_END, fsConstants.DATE_YEAR_END).toInt(2) + fsConstants.BASE_YEAR

fun String.parseHour(fsConstants: FsConstants): Int =
    substring(fsConstants.DATE_YEAR_END, fsConstants.DATE_HOUR_END).toInt(2)

fun String.parseMinute(fsConstants: FsConstants): Int =
    substring(fsConstants.DATE_HOUR_END, fsConstants.DATE_MINUTE_END).toInt(2)

fun String.parseSecond(fsConstants: FsConstants): Int =
    substring(fsConstants.DATE_MINUTE_END, fsConstants.DATE_SECOND_END).toInt(2)

fun String.toDateString(fsConstants: FsConstants): String =
    "${parseDay(fsConstants)}/${parseMonth(fsConstants)}/${parseYear(fsConstants)} " +
        "${parseHour(fsConstants)}:${parseMinute(fsConstants)}:${parseSecond(fsConstants)}"
```

- [ ] **Step 2: Replace BinaryFormat.kt** — `padStart`, `buildString`, `toString(2)`, `mapIndexed`:

```kotlin
package abstraction

interface BinaryFormat {
    fun padBinary(value: String, size: Int): String
    fun encodeName(name: String, maxChars: Int, targetBits: Int): String
    fun encodePermissionBits(permission: String): String
    fun applyPermissionBits(current: String, bits: String): String
    fun encodeDateBits(date: String): String
}

class DefaultBinaryFormat(
    private val fsConstants: FsConstants,
) : BinaryFormat {
    override fun padBinary(value: String, size: Int): String = value.padStart(size, '0')

    override fun encodeName(name: String, maxChars: Int, targetBits: Int): String {
        val limit = minOf(name.length, maxChars)
        return buildString {
            for (i in 0 until limit) {
                append((fsConstants.BYTE_PREFIX or name[i].code).toString(2).substring(1))
            }
        }.padEnd(targetBits, '0')
    }

    override fun encodePermissionBits(permission: String): String = buildString {
        for (i in fsConstants.PERMISSION_STRING_START until fsConstants.PERMISSION_STRING_START + fsConstants.PERMISSION_STRING_LENGTH) {
            append(if (permission[i] == '-') '0' else '1')
        }
    }

    override fun applyPermissionBits(current: String, bits: String): String {
        val permChars = "rxwrxwrxw"
        return current + bits.mapIndexed { i, bit -> if (bit == '1') permChars[i] else '-' }.joinToString("")
    }

    override fun encodeDateBits(date: String): String {
        val day = date.substring(fsConstants.DATE_STRING_DAY_START, fsConstants.DATE_STRING_DAY_END).toInt()
        val month = date.substring(fsConstants.DATE_STRING_MONTH_START, fsConstants.DATE_STRING_MONTH_END).toInt()
        val year = date.substring(fsConstants.DATE_STRING_YEAR_START, fsConstants.DATE_STRING_YEAR_END).toInt() - fsConstants.YEAR_OFFSET
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
```

- [ ] **Step 3: Replace Content.kt** — `toInt(2)`, `toString(2)`, `buildString`, `padEnd`:

```kotlin
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
        val block = if (content.length < fsConstants.CONTENT_CHUNK_CHARS) {
            buildString {
                append("11")
                val textBinary = buildString {
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
        val result = buildString {
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
```

- [ ] **Step 4: Update Pointers.kt stdlib calls** — `Integer.parseInt` → `.toInt(2)`, `Integer.toBinaryString` → `.toString(2)`, `ArrayList<String>` → `MutableList<String>`:

In `parseBinary`: change return type and `ArrayList<String>()` → `mutableListOf<String>()`:
```kotlin
fun parseBinary(binary: String): MutableList<String> {
    val childrenList = mutableListOf<String>()
    parent = binary.substring(fsConstants.POINTER_PARENT_START, fsConstants.POINTER_PARENT_END).toInt(2)
    childrenList.add("..-$parent")
    // ... rest of method unchanged except toInt(2) for position parsing
```

In `generateBinary`, change `Integer.toBinaryString` → `.toString(2)`:
```kotlin
val parentBinary = parent.toString(2)
// ...
binary.append(padBinary(Integer.toBinaryString(spaceManager.getFreePosition()), fsConstants.POINTER_BITS))
// becomes:
binary.append(padBinary(spaceManager.getFreePosition().toString(2), fsConstants.POINTER_BITS))
```

Also update the private `padBinary`:
```kotlin
private fun padBinary(s: String, size: Int): String = s.padStart(size, '0')
```

In `addChild`:
```kotlin
hardDisk.writePointer(
    padBinary(child.toString(2), fsConstants.POINTER_BITS),
    // ...
)
```

- [ ] **Step 5: Run checks**

```bash
./gradlew test detekt ktlintCheck
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add src/abstraction/BinaryDateExtensions.kt src/abstraction/BinaryFormat.kt \
        src/abstraction/Content.kt src/abstraction/Pointers.kt
git commit -m "refactor: replace Integer.parseInt/toBinaryString with Kotlin stdlib extensions"
```

---

## Task 4: FileEntry → Kotlin properties

**Files:**
- Modify: `src/abstraction/FileEntry.kt`
- Modify: `src/operations/KernelContext.kt` (call sites)
- Modify: `src/operations/CatOperation.kt`
- Modify: `src/operations/ChmodOperation.kt`
- Modify: `src/abstraction/Pointers.kt` (call sites)
- Modify: `test/operations/OperationsTest.kt`

- [ ] **Step 1: Replace FileEntry.kt** — remove all getters/setters, make fields `var` properties, use `buildString` and `.toInt(2)`/`.toString(2)`:

```kotlin
package abstraction

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FileEntry(
    private val binaryFormat: BinaryFormat,
    private val fsConstants: FsConstants,
    private val permissionUtils: PermissionUtils,
    private val content: Content,
) {
    var name: String = ""
    var date: String = ""
    var permission: String = "-"
    var parent: Int = 0
    var contentPointer: Int = 0
    var contentText: String = ""
    var currentPosition: Int = 0

    constructor(
        binaryFormat: BinaryFormat,
        fsConstants: FsConstants,
        permissionUtils: PermissionUtils,
        content: Content,
        name: String,
        contentText: String,
        parent: Int,
    ) : this(binaryFormat, fsConstants, permissionUtils, content) {
        this.name = name
        this.date = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault()).format(Date())
        this.permission = "-rx-r--r--"
        this.contentText = contentText
        this.parent = parent
    }

    fun updatePermission(position: Int) {
        permissionUtils.updatePermissionAt(position, permission)
    }

    fun parseBinary(binary: String) {
        var i = fsConstants.NAME_BITS_START
        while (i < fsConstants.NAME_BITS_END) {
            name += binary.substring(i, i + 8).toInt(2).toChar()
            i += 8
        }
        name = name.replace(0.toChar().toString(), "").replace("null", "")
        date += binary.toDateString(fsConstants)
        permission = binaryFormat.applyPermissionBits(
            permission,
            binary.substring(fsConstants.PERMISSION_BITS_START, fsConstants.PERMISSION_BITS_END),
        )
        parent = binary.substring(fsConstants.PARENT_POINTER_START, fsConstants.PARENT_POINTER_END).toInt(2)
        contentPointer = binary.substring(fsConstants.CHILD_POINTER_START, fsConstants.CHILD_POINTER_END).toInt(2)
    }

    fun generateBinary(): String = buildString {
        append("01")
        append(binaryFormat.encodeName(name, fsConstants.NAME_MAX_CHARS, fsConstants.NAME_BITS_END))
        append(binaryFormat.encodeDateBits(date))
        append(binaryFormat.encodePermissionBits(permission))
        append(binaryFormat.padBinary(parent.toString(2), fsConstants.POINTER_BITS))
        val contentPosition = content.generateBinary(contentText)
        append(binaryFormat.padBinary(contentPosition.toString(2), fsConstants.POINTER_BITS))
    }
}
```

- [ ] **Step 2: Update call sites in KernelContext.kt**

In `listDirectoryDetailed`, replace:
```kotlin
// Before:
output.append(file.getPermission())
output.append(" ")
output.append(file.getDate())
// After:
output.append(file.permission)
output.append(" ")
output.append(file.date)
```

In `dumpDirectory`, replace:
```kotlin
// Before:
output.append(content.parseBinary(hardDisk.readBlock(file.getContentPointer())))
// After:
output.append(content.parseBinary(hardDisk.readBlock(file.contentPointer)))
```

- [ ] **Step 3: Update CatOperation.kt**

```kotlin
// Before:
result = context.content.parseBinary(context.hardDisk.readBlock(file.getContentPointer()))
// After:
result = context.content.parseBinary(context.hardDisk.readBlock(file.contentPointer))
```

- [ ] **Step 4: Update ChmodOperation.kt**

```kotlin
// Before:
file.setCurrentPosition(split[1].toInt())
file.setPermission(permissionString.toString())
file.updatePermission(split[1].toInt())
// After:
file.currentPosition = split[1].toInt()
file.permission = permissionString
file.updatePermission(split[1].toInt())
```

Note: `permissionString` is now a `String` (will be after Task 7), for now keep `.toString()` if it's still a `StringBuilder`.

- [ ] **Step 5: Update Pointers.kt call sites**

In `parseBinary` and `loadMoreChildren`, replace:
```kotlin
// Before:
entry.getName().replace(0.toChar().toString(), "")
// After:
entry.name.replace(0.toChar().toString(), "")
```

- [ ] **Step 6: Update OperationsTest.kt**

```kotlin
// Before:
every { file.getContentPointer() } returns 9
// After:
every { file.contentPointer } returns 9
```

- [ ] **Step 7: Run checks**

```bash
./gradlew test detekt ktlintCheck
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
git add src/abstraction/FileEntry.kt src/operations/KernelContext.kt \
        src/operations/CatOperation.kt src/operations/ChmodOperation.kt \
        src/abstraction/Pointers.kt test/operations/OperationsTest.kt
git commit -m "refactor: replace FileEntry getters/setters with Kotlin properties"
```

---

## Task 5: CurrentDirectory → Kotlin properties

**Files:**
- Modify: `src/abstraction/CurrentDirectory.kt`
- Modify: `src/abstraction/Pointers.kt`
- Modify: `src/operations/KernelContext.kt`
- Modify: `src/operations/CatOperation.kt`
- Modify: `src/operations/CdOperation.kt`
- Modify: `src/operations/ChmodOperation.kt`
- Modify: `src/operations/CreateFileOperation.kt`
- Modify: `src/operations/LsOperation.kt`
- Modify: `src/operations/MkdirOperation.kt`
- Modify: `src/operations/RmdirOperation.kt`
- Modify: `test/operations/OperationsTest.kt`

- [ ] **Step 1: Replace CurrentDirectory.kt**

```kotlin
package abstraction

import hardware.HardDisk
import infra.Logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CurrentDirectory(
    private val hardDisk: HardDisk,
    private val spaceManager: SpaceManager,
    private val fsConstants: FsConstants,
    private val binaryFormat: BinaryFormat,
    private val permissionUtils: PermissionUtils,
    private val content: Content,
    private val logger: Logger,
    name: String? = null,
    parent: Int? = null,
) {
    var name: String = ""
    var date: String = ""
    var permission: String = "-"
    var parent: Int = 0
    var childrenPointer: Int = 0
    var childrenPointerPosition: Int = 0
    var parentPosition: Int = 0
    var currentPosition: Int = 0

    init {
        if (name != null && parent != null) {
            this.parent = parent
            this.name = name
            this.date = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale.getDefault()).format(Date())
            this.permission = "-rx-r--r--"
            currentPosition = spaceManager.getFreePosition()
            parentPosition = if (name == "/") 0 else parent
            hardDisk.writeBlock(generateBinary(), currentPosition)
            createChildrenPointerBlock()
        }
    }

    fun parseBinary(binary: String) {
        var i = fsConstants.NAME_BITS_START
        while (i < fsConstants.NAME_BITS_END) {
            name += binary.substring(i, i + 8).toInt(2).toChar()
            i += 8
        }
        name = name.replace(0.toChar().toString(), "")
        date += binary.toDateString(fsConstants)
        permission = binaryFormat.applyPermissionBits(
            permission,
            binary.substring(fsConstants.PERMISSION_BITS_START, fsConstants.PERMISSION_BITS_END),
        )
        parent = binary.substring(fsConstants.PARENT_POINTER_START, fsConstants.PARENT_POINTER_END).toInt(2)
        childrenPointer = binary.substring(fsConstants.CHILD_POINTER_START, fsConstants.CHILD_POINTER_END).toInt(2)
    }

    fun updatePermission(position: Int) {
        permissionUtils.updatePermissionAt(position, permission)
    }

    fun generateBinary(): String = buildString {
        append("00")
        append(binaryFormat.encodeName(name, fsConstants.NAME_MAX_CHARS, fsConstants.NAME_BITS_END))
        append(binaryFormat.encodeDateBits(date))
        append(binaryFormat.encodePermissionBits(permission))
        append(binaryFormat.padBinary(parentPosition.toString(2), fsConstants.POINTER_BITS))
        childrenPointerPosition = spaceManager.getFreePosition()
        append(binaryFormat.padBinary(childrenPointerPosition.toString(2), fsConstants.POINTER_BITS))
        val dir = CurrentDirectory(hardDisk, spaceManager, fsConstants, binaryFormat, permissionUtils, content, logger)
        dir.parseBinary(toString())
    }

    private fun createChildrenPointerBlock() {
        val pointers = Pointers(
            childrenPointerPosition,
            hardDisk,
            fsConstants,
            spaceManager,
            binaryFormat,
            permissionUtils,
            content = content,
            logger = logger,
        )
        pointers.parent = parentPosition
        hardDisk.writeBlock(pointers.generateBinary(), childrenPointerPosition)
    }
}
```

- [ ] **Step 2: Update Pointers.kt** — `entry.getName()` → `entry.name` for CurrentDirectory entries (already done for FileEntry in Task 4):

```kotlin
// Before:
val result = entry.getName().replace(0.toChar().toString(), "") + "-" + position + "-" + (i - fsConstants.POINTER_USED_START)
// After:
val result = entry.name.replace(0.toChar().toString(), "") + "-" + position + "-" + (i - fsConstants.POINTER_USED_START)
```

Also update `setParent` → property access:
```kotlin
// Before (in createChildrenPointerBlock of CurrentDirectory — already handled above):
pointers.setParent(getParentPosition())
// After:
pointers.parent = parentPosition
```

- [ ] **Step 3: Update KernelContext.kt call sites**

Replace all CurrentDirectory getter/setter calls:

| Before | After |
|--------|-------|
| `current.getChildrenPointer()` | `current.childrenPointer` |
| `current.getChildrenPointerPosition()` | `current.childrenPointerPosition` |
| `current.getParent()` | `current.parent` |
| `current.getName()` | `current.name` |
| `directory.getPermission()` | `directory.permission` |
| `directory.getDate()` | `directory.date` |

Apply these across all methods in `DefaultKernelContext`: `listDirectoryDetailed`, `findChildDirectoryPointer`, `currentPath`, `resolveDirectoryPointer`, `dumpDirectory`.

- [ ] **Step 4: Update remaining operation files**

**CatOperation.kt:**
```kotlin
// Before:
val pointers = context.newPointers(current.getChildrenPointer())
val children = pointers.parseBinary(context.hardDisk.readBlock(current.getChildrenPointer()))
// After:
val pointers = context.newPointers(current.childrenPointer)
val children = pointers.parseBinary(context.hardDisk.readBlock(current.childrenPointer))
```

**CdOperation.kt:**
```kotlin
// Before:
positionAux = current.getParent()
// After:
positionAux = current.parent
```

**ChmodOperation.kt:**
```kotlin
// Before:
val pointers = context.newPointers(current.getChildrenPointer())
val children = pointers.parseBinary(context.hardDisk.readBlock(current.getChildrenPointer()))
// ...
context.log("Directory name " + directory.getName())
directory.setPermission(permissionString.toString())
directory.updatePermission(split[1].toInt())
// After:
val pointers = context.newPointers(current.childrenPointer)
val children = pointers.parseBinary(context.hardDisk.readBlock(current.childrenPointer))
// ...
context.log("Directory name ${directory.name}")
directory.permission = permissionString.toString()
directory.updatePermission(split[1].toInt())
```

**CreateFileOperation.kt:**
```kotlin
// Before:
val pointers = context.newPointers(current.getChildrenPointer())
val children = pointers.parseBinary(context.hardDisk.readBlock(current.getChildrenPointer()))
// ...
val file = context.newFileEntry(fileName, finalContent, current.getCurrentPosition())
// After:
val pointers = context.newPointers(current.childrenPointer)
val children = pointers.parseBinary(context.hardDisk.readBlock(current.childrenPointer))
// ...
val file = context.newFileEntry(fileName, finalContent, current.currentPosition)
```

**LsOperation.kt:**
```kotlin
// Before:
val pointers = context.newPointers(current.getChildrenPointer())
val children = pointers.parseBinary(context.hardDisk.readBlock(current.getChildrenPointer()))
// After:
val pointers = context.newPointers(current.childrenPointer)
val children = pointers.parseBinary(context.hardDisk.readBlock(current.childrenPointer))
```

**MkdirOperation.kt:**
```kotlin
// Before:
val pointers = context.newPointers(current.getChildrenPointer())
val children = pointers.parseBinary(context.hardDisk.readBlock(current.getChildrenPointer()))
// ...
val positionToAdd = child.getCurrentPosition()
// ...
val parentPointers = context.newPointers(parent.getChildrenPointer())
// After:
val pointers = context.newPointers(current.childrenPointer)
val children = pointers.parseBinary(context.hardDisk.readBlock(current.childrenPointer))
// ...
val positionToAdd = child.currentPosition
// ...
val parentPointers = context.newPointers(parent.childrenPointer)
```

**RmdirOperation.kt:**
```kotlin
// Before:
val pointers = context.newPointers(current.getChildrenPointer())
val children = pointers.parseBinary(context.hardDisk.readBlock(current.getChildrenPointer()))
// After:
val pointers = context.newPointers(current.childrenPointer)
val children = pointers.parseBinary(context.hardDisk.readBlock(current.childrenPointer))
```

- [ ] **Step 5: Update OperationsTest.kt**

```kotlin
// Before:
every { current.getChildrenPointer() } returns 1
every { parent.getChildrenPointer() } returns 2
every { child.getCurrentPosition() } returns 5
// After:
every { current.childrenPointer } returns 1
every { parent.childrenPointer } returns 2
every { child.currentPosition } returns 5
```

- [ ] **Step 6: Run checks**

```bash
./gradlew test detekt ktlintCheck
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add src/abstraction/CurrentDirectory.kt src/abstraction/Pointers.kt \
        src/operations/KernelContext.kt src/operations/CatOperation.kt \
        src/operations/CdOperation.kt src/operations/ChmodOperation.kt \
        src/operations/CreateFileOperation.kt src/operations/LsOperation.kt \
        src/operations/MkdirOperation.kt src/operations/RmdirOperation.kt \
        test/operations/OperationsTest.kt
git commit -m "refactor: replace CurrentDirectory getters/setters with Kotlin properties"
```

---

## Task 6: FileSystemSimulator.dispatchCommand → when

**Files:**
- Modify: `src/operatingSystem/fileSystem/FileSystemSimulator.kt`

- [ ] **Step 1: Replace dispatchCommand and update history field**

Change the `history` field:
```kotlin
// Before:
private var history: ArrayList<String> = ArrayList()
// After:
private var history: MutableList<String> = mutableListOf()
```

Remove `import java.util.ArrayList`.

Replace `dispatchCommand`:
```kotlin
private fun dispatchCommand(command: String) {
    if (command.trim().isEmpty()) return
    val args = command.trim().split(" ")
    val arg = if (args.size > 1) command.trim().substringAfter("${args[0]} ") else ""
    when (args[0]) {
        "cd" -> lastResult = myKernel!!.cd(arg)
        "ls" -> lastResult = myKernel!!.ls(arg)
        "mkdir" -> lastResult = myKernel!!.mkdir(arg)
        "rmdir" -> lastResult = myKernel!!.rmdir(arg)
        "cp" -> lastResult = myKernel!!.cp(arg)
        "mv" -> lastResult = myKernel!!.mv(arg)
        "rm" -> lastResult = myKernel!!.rm(arg)
        "chmod" -> lastResult = myKernel!!.chmod(arg)
        "createfile" -> lastResult = myKernel!!.createfile(arg)
        "clear" -> textArea.text = ""
        "cat" -> lastResult = myKernel!!.cat(arg)
        "batch" -> lastResult = myKernel!!.batch(arg)
        "dump" -> lastResult = if (args.size > 1) myKernel!!.dump(arg) else myKernel!!.batch("")
        "info" -> lastResult = myKernel!!.info()
        "exit" -> System.exit(0)
        else -> lastResult = "$command: Invalid command."
    }
}
```

- [ ] **Step 2: Run checks**

```bash
./gradlew test detekt ktlintCheck
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add src/operatingSystem/fileSystem/FileSystemSimulator.kt
git commit -m "refactor: replace dispatchCommand if/else chain with when expression"
```

---

## Task 7: ChmodOperation buildString + resolveDirectoryPointer no-op removal

**Files:**
- Modify: `src/operations/ChmodOperation.kt`
- Modify: `src/operations/KernelContext.kt`

- [ ] **Step 1: Update ChmodOperation.kt** — replace `StringBuilder("-")` with `buildString`:

```kotlin
val permissionString = buildString {
    append("-")
    for (i in 0..2) {
        append(
            when (permissionBits[i]) {
                '0' -> "---"
                '1' -> "--w"
                '2' -> "-x-"
                '3' -> "-xw"
                '4' -> "r--"
                '5' -> "r-w"
                '6' -> "rx-"
                '7' -> "rxw"
                else -> {
                    context.log("Permission error")
                    result = "Permission error"
                    ""
                }
            },
        )
    }
}
context.log(permissionString)
```

Also update the remaining setter call that still uses `.toString()`:
```kotlin
// Before:
directory.permission = permissionString.toString()
file.permission = permissionString.toString()
// After (permissionString is now String):
directory.permission = permissionString
file.permission = permissionString
```

- [ ] **Step 2: Remove no-op branch in KernelContext.resolveDirectoryPointer**

In `DefaultKernelContext.resolveDirectoryPointer`, remove the `"." ->` branch entirely:

```kotlin
// Before:
"." -> {
    currentPointer = currentPointer
}
// After: (delete this branch)
```

- [ ] **Step 3: Run checks**

```bash
./gradlew test detekt ktlintCheck
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add src/operations/ChmodOperation.kt src/operations/KernelContext.kt
git commit -m "refactor: buildString in ChmodOperation, remove no-op branch in resolveDirectoryPointer"
```

---

## Task 8: Pointers — extract entryName helper + buildString in KernelContext

**Files:**
- Modify: `src/abstraction/Pointers.kt`
- Modify: `src/operations/KernelContext.kt`

- [ ] **Step 1: Extract entryName helper and update Pointers.kt**

Add a private helper that eliminates the duplicated `if (block == "00") / if (block == "01")` branches in both `parseBinary` and `loadMoreChildren`:

```kotlin
private fun entryName(block: String): String? = when (block.substring(0, 2)) {
    "00" -> {
        val entry = CurrentDirectory(hardDisk, spaceManager, fsConstants, binaryFormat, permissionUtils, content, logger)
        entry.parseBinary(block)
        entry.name.replace(0.toChar().toString(), "")
    }
    "01" -> {
        val entry = FileEntry(binaryFormat, fsConstants, permissionUtils, content)
        entry.parseBinary(block)
        entry.name.replace(0.toChar().toString(), "")
    }
    else -> null
}
```

In `parseBinary`, replace the two `if` blocks with:
```kotlin
for (i in fsConstants.POINTER_USED_START until fsConstants.POINTER_USED_START + fsConstants.POINTER_USED_COUNT) {
    if (binary[i] == '1') {
        val slotIndex = i - fsConstants.POINTER_USED_START
        val initial = slotIndex * fsConstants.POINTER_BITS + fsConstants.POINTER_CHILDREN_START
        usedSlots[slotIndex] = true
        val position = binary.substring(initial, initial + fsConstants.POINTER_BITS).toInt(2)
        val block = hardDisk.readBlock(position)
        entryName(block)?.let { name ->
            childrenList.add("$name-$position-$slotIndex")
        }
    }
}
```

In `loadMoreChildren`, replace similarly (no slotIndex in result for directories, and note the existing file result uses `nextPointers.children[position]` — preserve that):
```kotlin
for (i in fsConstants.POINTER_USED_START until fsConstants.POINTER_USED_START + fsConstants.POINTER_USED_COUNT) {
    if (binary[i] == '1') {
        val slotIndex = i - fsConstants.POINTER_USED_START
        val inicio = slotIndex * fsConstants.POINTER_BITS + fsConstants.POINTER_CHILDREN_START
        nextPointers.usedSlots[slotIndex] = true
        val position = binary.substring(inicio, inicio + fsConstants.POINTER_BITS).toInt(2)
        val block = hardDisk.readBlock(position)
        entryName(block)?.let { name ->
            childrenList.add("$name-$position")
        }
    }
}
```

Also update `generateBinary` to use `buildString`:
```kotlin
fun generateBinary(): String = buildString {
    append("10")
    append(padBinary(parent.toString(2), fsConstants.POINTER_BITS))
    val usedBits = buildString { usedSlots.forEach { append(if (it) '1' else '0') } }
    val childrenBits = buildString {
        for (i in 0 until fsConstants.POINTERS_COUNT) {
            if (usedSlots[i]) append(padBinary(children[i].toString(2), fsConstants.POINTER_BITS))
            else append(emptyPointer())
        }
    }
    append(usedBits)
    append(childrenBits)
    if (hasMore) {
        append("1")
        append(padBinary(spaceManager.getFreePosition().toString(2), fsConstants.POINTER_BITS))
    } else {
        append("0")
        append(emptyPointer())
    }
}
```

- [ ] **Step 2: Use buildString in KernelContext.listDirectoryDetailed and dumpDirectory**

```kotlin
override fun listDirectoryDetailed(directoryPointer: Int): String = buildString {
    val current = newCurrentDirectory()
    current.parseBinary(hardDisk.readBlock(directoryPointer))
    val pointers = newPointers(current.childrenPointer)
    val children = pointers.parseBinary(hardDisk.readBlock(current.childrenPointer))
    for (child in children) {
        val split = child.split("-")
        if (split[0].contains(".txt")) {
            val file = newFileEntry()
            file.parseBinary(hardDisk.readBlock(split[1].toInt()))
            append("${split[0]} ${file.permission} ${file.date}\n")
        } else {
            val directory = newCurrentDirectory()
            directory.parseBinary(hardDisk.readBlock(split[1].toInt()))
            append("${split[0]} ${directory.permission} ${directory.date}\n")
        }
    }
}
```

Also replace `fileDoesNotExist` with `none {}`:
```kotlin
override fun fileDoesNotExist(children: List<String>, name: String): Boolean =
    children.none { it.split("-")[0] == name }
```

Update the `KernelContext` interface parameter type accordingly:
```kotlin
fun fileDoesNotExist(children: List<String>, name: String): Boolean
```

- [ ] **Step 3: Run checks**

```bash
./gradlew test detekt ktlintCheck
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: Commit**

```bash
git add src/abstraction/Pointers.kt src/operations/KernelContext.kt
git commit -m "refactor: extract entryName helper in Pointers, buildString in KernelContext"
```

---

## Self-Review

**Spec coverage:**
- Section 1 (Properties & Accessors) → Tasks 4, 5 ✓
- Section 2 (Standard Library) → Task 3 ✓
- Section 3 (Binary.kt) → Task 2 ✓
- Section 4 (Control Flow) → Tasks 6, 7 ✓
- Section 5 (Pointers DRY) → Task 8 ✓
- Section 6 (Kernel KDoc) → Task 1 ✓

**Constraints check:**
- Tests must pass → each task ends with `./gradlew test` ✓
- detekt + ktlint must pass → checked in every task ✓
- No logic changes → all spec "logic unchanged" constraints met ✓
- `FileSystemSimulator` Swing boilerplate not touched → only `dispatchCommand` and `history` field modified ✓

**Type consistency:**
- `Pointers.parseBinary` returns `MutableList<String>` (Task 3) — callers in KernelContext use it as `val children = ...` and iterate, which is compatible ✓
- `fileDoesNotExist(children: List<String>)` (Task 8) — callers pass `MutableList<String>` which is a `List<String>` ✓
- `permissionString` becomes `String` in Task 7 — `.toString()` calls on it removed ✓
- `entryName` helper added in Task 8 — `Pointers` already has access to all required fields ✓
