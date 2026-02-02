package operations

import abstraction.CurrentDirectory
import abstraction.FileEntry
import abstraction.Pointers
import hardware.HardDisk
import infra.Logger
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class OperationsTest {
    @Test
    fun lsOperationListsWithLongFormat() {
        val context = mockContext()
        every { context.currentDiskPosition } returns 0
        every { context.listDirectoryDetailed(0) } returns "detailed"

        val result = LsOperation(context).execute("-l")

        assertEquals("detailed", result)
        verify(exactly = 1) { context.listDirectoryDetailed(0) }
    }

    @Test
    fun mkdirOperationCreatesWhenMissing() {
        val context = mockContext()
        val hardDisk = context.hardDisk
        val current = mockk<CurrentDirectory>(relaxed = true)
        val parent = mockk<CurrentDirectory>(relaxed = true)
        val child = mockk<CurrentDirectory>(relaxed = true)
        val pointers = mockk<Pointers>(relaxed = true)
        val parentPointers = mockk<Pointers>(relaxed = true)

        every { context.currentDiskPosition } returns 0
        every { context.newCurrentDirectory() } returnsMany listOf(current, parent)
        every { context.newCurrentDirectory("dir", 0) } returns child
        every { hardDisk.readBlock(any()) } returns "block"
        every { current.getChildrenPointer() } returns 1
        every { parent.getChildrenPointer() } returns 2
        every { child.getCurrentPosition() } returns 5
        every { context.newPointers(1) } returns pointers
        every { context.newPointers(2) } returns parentPointers
        every { pointers.parseBinary(any()) } returns arrayListOf()
        every { parentPointers.addChild(5) } returns "added"

        val result = MkdirOperation(context).execute("dir")

        assertEquals("added", result)
        verify(exactly = 1) { parentPointers.addChild(5) }
    }

    @Test
    fun cdOperationChangesDirectory() {
        val context = mockContext()
        var currentPosition = 1
        every { context.currentDiskPosition } answers { currentPosition }
        every { context.currentDiskPosition = any() } answers { currentPosition = arg(0) }
        every { context.currentPath(0) } returns ""

        val result = CdOperation(context).execute("/")

        assertEquals("", result)
        assertEquals(0, currentPosition)
    }

    @Test
    fun rmdirOperationRejectsNonEmptyDirectory() {
        val context = mockContext()
        val hardDisk = context.hardDisk
        val current = mockk<CurrentDirectory>(relaxed = true)
        val pointers = mockk<Pointers>(relaxed = true)

        every { context.currentDiskPosition } returns 0
        every { context.resolveDirectoryPointer(0, "dir") } returns 3
        every { context.newCurrentDirectory() } returns current
        every { current.getChildrenPointer() } returns 4
        every { context.newPointers(4) } returns pointers
        every { hardDisk.readBlock(any()) } returns "block"
        every { pointers.parseBinary(any()) } returns arrayListOf("..-0", "child-1-0")

        val result = RmdirOperation(context).execute("dir")

        assertEquals("Directory contains files and/or subdirectories", result)
    }

    @Test
    fun cpOperationLogsAndReturnsEmpty() {
        val context = mockContext()

        val result = CpOperation(context).execute("a b")

        assertEquals("", result)
        verify { context.log("System call: cp") }
    }

    @Test
    fun mvOperationLogsAndReturnsEmpty() {
        val context = mockContext()

        val result = MvOperation(context).execute("a b")

        assertEquals("", result)
        verify { context.log("System call: mv") }
    }

    @Test
    fun rmOperationDelegatesToRmdir() {
        val context = mockContext()
        val rmdirOperation = mockk<RmdirOperation>()
        every { rmdirOperation.execute(any()) } returns ""

        val result = RmOperation(context, rmdirOperation).execute("-r dir")

        assertEquals("", result)
        verify(exactly = 2) { rmdirOperation.execute(" dir") }
    }

    @Test
    fun chmodOperationUpdatesChildrenWhenFound() {
        val context = mockContext()
        val hardDisk = context.hardDisk
        val current = mockk<CurrentDirectory>(relaxed = true)
        val pointers = mockk<Pointers>(relaxed = true)

        every { context.currentDiskPosition } returns 0
        every { context.resolveDirectoryPointer(0, "/home") } returns 7
        every { context.newCurrentDirectory() } returns current
        every { current.getChildrenPointer() } returns 9
        every { context.newPointers(9) } returns pointers
        every { hardDisk.readBlock(any()) } returns "block"
        every { pointers.parseBinary(any()) } returns arrayListOf()

        val result = ChmodOperation(context).execute("777 /home/file.txt")

        assertEquals("", result)
        verify { context.log("System call: chmod") }
    }

    @Test
    fun createFileOperationReturnsAlreadyExists() {
        val context = mockContext()
        val hardDisk = context.hardDisk
        val current = mockk<CurrentDirectory>(relaxed = true)
        val pointers = mockk<Pointers>(relaxed = true)

        every { context.currentDiskPosition } returns 0
        every { context.resolveDirectoryPointer(0, "file.txt") } returns 3
        every { context.newCurrentDirectory() } returns current
        every { current.getChildrenPointer() } returns 6
        every { context.newPointers(6) } returns pointers
        every { hardDisk.readBlock(any()) } returns "block"
        every { pointers.parseBinary(any()) } returns arrayListOf()
        every { context.fileDoesNotExist(any(), "file.txt") } returns false

        val result = CreateFileOperation(context).execute("file.txt content")

        assertEquals("File already exists.", result)
    }

    @Test
    fun catOperationReadsFileContent() {
        val context = mockContext()
        val hardDisk = context.hardDisk
        val current = mockk<CurrentDirectory>(relaxed = true)
        val pointers = mockk<Pointers>(relaxed = true)
        val file = mockk<FileEntry>(relaxed = true)

        every { context.currentDiskPosition } returns 0
        every { context.resolveDirectoryPointer(0, "") } returns 2
        every { context.newCurrentDirectory() } returns current
        every { current.getChildrenPointer() } returns 4
        every { context.newPointers(4) } returns pointers
        every { hardDisk.readBlock(any()) } returns "block"
        every { pointers.parseBinary(any()) } returns arrayListOf("file.txt-3-0")
        every { context.newFileEntry() } returns file
        every { file.getContentPointer() } returns 9
        every { context.content.parseBinary(any()) } returns "content"

        val result = CatOperation(context).execute("file.txt")

        assertEquals("content", result)
    }

    @Test
    fun batchOperationDispatchesCommands() {
        val context = mockContext()
        val cdOperation = mockk<CdOperation>()
        val mkdirOperation = mockk<MkdirOperation>()
        val lsOperation = mockk<LsOperation>()
        val createFileOperation = mockk<CreateFileOperation>()
        val chmodOperation = mockk<ChmodOperation>()
        val rmOperation = mockk<RmOperation>()
        val dumpOperation = mockk<DumpOperation>()
        val script =
            File.createTempFile("batch", ".txt").apply {
                writeText(
                    """
                    cd /
                    mkdir test
                    ls
                    createfile test.txt hello
                    chmod 777 test.txt
                    rm -r test
                    dump dump.txt
                    """.trimIndent(),
                )
                deleteOnExit()
            }

        every { cdOperation.execute(any()) } returns ""
        every { mkdirOperation.execute(any()) } returns ""
        every { lsOperation.execute(any()) } returns ""
        every { createFileOperation.execute(any()) } returns ""
        every { chmodOperation.execute(any()) } returns ""
        every { rmOperation.execute(any()) } returns ""
        every { dumpOperation.execute(any()) } returns ""

        BatchOperation(
            context,
            cdOperation,
            mkdirOperation,
            lsOperation,
            createFileOperation,
            chmodOperation,
            rmOperation,
            dumpOperation,
        ).execute(script.absolutePath)

        verify { cdOperation.execute("/") }
        verify { mkdirOperation.execute("test") }
        verify { lsOperation.execute("ls") }
        verify { createFileOperation.execute("test.txt hello") }
        verify { chmodOperation.execute(" 777 test.txt") }
        verify { rmOperation.execute(" -r test") }
        verify { dumpOperation.execute(match { it.contains(".txt") }) }
    }

    @Test
    fun dumpOperationWritesOutput() {
        val context = mockContext()
        val file = File.createTempFile("dump", ".txt").apply { deleteOnExit() }
        every { context.dumpDirectory(0, any()) } answers {
            arg<StringBuilder>(1).append("mkdir test\n")
        }

        val result = DumpOperation(context).execute(file.absolutePath)

        assertEquals("", result)
        assertEquals("mkdir test\n", file.readText())
    }

    @Test
    fun infoOperationReturnsMetadata() {
        val context = mockContext()

        val result = InfoOperation(context).execute("")

        assertEquals(true, result.contains("Student name:"))
        verify { context.log("System call: info") }
    }

    private fun mockContext(): KernelContext {
        val logger = mockk<Logger>(relaxed = true)
        val hardDisk = mockk<HardDisk>(relaxed = true)
        return mockk(relaxed = true) {
            every { this@mockk.logger } returns logger
            every { this@mockk.hardDisk } returns hardDisk
            every { log(any()) } just Runs
        }
    }
}
