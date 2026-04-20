# Idiomatic Kotlin Migration Design

**Date:** 2026-04-20
**Scope:** All source files under `src/` and `test/`
**Goal:** Replace Java-influenced patterns with idiomatic Kotlin. Logic unchanged. Detekt + ktlint must pass. Tests must pass.

---

## Context

The project is already written in Kotlin but follows Java idioms throughout: explicit getters/setters, `companion object` + `@JvmStatic`, `ArrayList`, `Integer.parseInt`, manual string accumulation, and long `if/else if` chains. This migration makes the code read and behave as Kotlin-first without changing any business logic.

---

## Section 1: Properties & Accessors

**Files:** `abstraction/FileEntry.kt`, `abstraction/CurrentDirectory.kt`

Replace every Java-style getter (`getName()`, `getDate()`, etc.) and setter (`setName()`, `setDate()`, etc.) with Kotlin `var` properties. Secondary constructors that set fields via `this.x = x` assignments migrate naturally — the fields become property initialisers or remain `var` with default values.

All call sites update from `entry.getName()` → `entry.name`, `entry.setPermission(s)` → `entry.permission = s`.

`parseBinary` and `generateBinary` reference these fields internally and need no structural changes, only accessor syntax updates.

---

## Section 2: Standard Library

**Files:** All files with binary manipulation or collection construction.

| Before | After |
|--------|-------|
| `Integer.parseInt(s, 2)` | `s.toInt(2)` |
| `Integer.toBinaryString(n)` | `n.toString(2)` |
| `ArrayList<String>()` | `mutableListOf<String>()` |
| `var r = ""; r += x; return r` | `buildString { append(x) }` or direct return |
| `StringBuilder(); .append(); .toString()` | `buildString { append() }` |

`BooleanArray` and `IntArray` are kept — they are fixed-size primitive arrays and appropriate here.

Loop patterns that accumulate a boolean flag (`var found = false; for (...) { if (...) found = true }`) are replaced with `any {}`, `none {}`, or `firstOrNull {}` where the intent is clearer.

---

## Section 3: Binary.kt

**File:** `binary/Binary.kt`

The `Binary` class is a stateless utility — all methods are `@JvmStatic` in a `companion object`, which is the Java pattern for static utilities. There is no Java interop in this project, so the wrapper disappears.

**Changes:**
- Delete the `class Binary` and `companion object`
- All methods become top-level functions in `binary/Binary.kt`
- `private const val` constants stay at file level
- `UNSIGNED_BASE`, `chars`, `HEX_PREFIX`, `HEX_PREFIX_LEN`, `HEX_INT_LENGTH`, `HEX_LONG_LENGTH` become top-level `private` declarations
- Call sites update from `Binary.intToBinaryString(x)` → `intToBinaryString(x)` (or keep qualified if needed for clarity)
- The `binary: Binary` field in `KernelContext` / `DefaultKernelContext` is removed since there is no instance state

---

## Section 4: Control Flow

**Files:** `operatingSystem/fileSystem/FileSystemSimulator.kt`, `operations/ChmodOperation.kt`, `operations/KernelContext.kt`

**`dispatchCommand` in FileSystemSimulator:**
Replace the 15-branch `if/else if` chain with a `when (args[0])` expression. Each branch extracts its argument with `command.trim().substringAfter("${args[0]} ").trim()` instead of hardcoded `substring` offsets, preserving the same trimming behaviour.

**`ChmodOperation`:**
The `when (permissionBits[i])` block appends to a `StringBuilder` — replace with `buildString {}` wrapping the `when` expression that returns the three-char segment per group.

**`resolveDirectoryPointer` in KernelContext:**
Remove the `"." -> { currentPointer = currentPointer }` no-op branch.

---

## Section 5: Pointers — DRY Block Parsing

**File:** `abstraction/Pointers.kt`

`parseBinary` and `loadMoreChildren` both iterate the used-slot bits of a binary block and decode each child into either a `CurrentDirectory` or `FileEntry`. The logic is duplicated.

**Change:** Extract `private fun collectChildren(binary: String, childrenList: MutableList<String>)` that performs the slot iteration and block-type dispatch. Both `parseBinary` and `loadMoreChildren` call this helper. `loadMoreChildren` becomes a thin recursive wrapper.

---

## Section 6: Kernel KDoc

**File:** `operatingSystem/Kernel.kt`

Remove all KDoc blocks. Every method name (`ls`, `mkdir`, `cd`, `rmdir`, `cp`, `mv`, `rm`, `chmod`, `createfile`, `cat`, `batch`, `dump`, `info`) is self-describing. The `@param parameters` and `@return` lines repeat identically for every method and add no information.

---

## Constraints

- All 12 existing tests must pass after migration
- `./gradlew detekt` must produce no violations
- `./gradlew ktlintCheck` must produce no violations
- No logic changes — only syntax and idiom
- `FileSystemSimulator` Swing boilerplate (`initComponents`, layout code) is not touched

---

## Out of Scope

- Adding new features or operations
- Changing the DI setup (Koin module)
- Modifying test structure beyond updating call sites to use properties
- Converting `MvOperation` or `CpOperation` stubs into real implementations
