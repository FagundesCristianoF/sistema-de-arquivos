package main

import operatingSystem.Kernel
import operations.BatchOperation
import operations.CatOperation
import operations.CdOperation
import operations.ChmodOperation
import operations.CpOperation
import operations.CreateFileOperation
import operations.DumpOperation
import operations.InfoOperation
import operations.LsOperation
import operations.MkdirOperation
import operations.MvOperation
import operations.RmOperation
import operations.RmdirOperation

class MyKernel(
    private val lsOperation: LsOperation,
    private val mkdirOperation: MkdirOperation,
    private val cdOperation: CdOperation,
    private val rmdirOperation: RmdirOperation,
    private val cpOperation: CpOperation,
    private val mvOperation: MvOperation,
    private val rmOperation: RmOperation,
    private val chmodOperation: ChmodOperation,
    private val createFileOperation: CreateFileOperation,
    private val catOperation: CatOperation,
    private val batchOperation: BatchOperation,
    private val dumpOperation: DumpOperation,
    private val infoOperation: InfoOperation,
) : Kernel {
    override fun ls(parameters: String): String = lsOperation.execute(parameters)

    override fun mkdir(parameters: String): String = mkdirOperation.execute(parameters)

    override fun cd(parameters: String): String = cdOperation.execute(parameters)

    override fun rmdir(parameters: String): String = rmdirOperation.execute(parameters)

    override fun cp(parameters: String): String = cpOperation.execute(parameters)

    override fun mv(parameters: String): String = mvOperation.execute(parameters)

    override fun rm(parameters: String): String = rmOperation.execute(parameters)

    override fun chmod(parameters: String): String = chmodOperation.execute(parameters)

    override fun createfile(parameters: String): String = createFileOperation.execute(parameters)

    override fun cat(parameters: String): String = catOperation.execute(parameters)

    override fun batch(parameters: String): String = batchOperation.execute(parameters)

    override fun dump(parameters: String): String = dumpOperation.execute(parameters)

    override fun info(): String = infoOperation.execute("")
}
