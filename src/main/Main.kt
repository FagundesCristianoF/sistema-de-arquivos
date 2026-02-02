package main

import di.appModule
import infra.Logger
import operatingSystem.Kernel
import operatingSystem.fileSystem.FileSystemSimulator
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get

fun main() {
    startKoin {
        modules(appModule)
    }
    val kernel: Kernel = get(Kernel::class.java)
    val logger: Logger = get(Logger::class.java)
    FileSystemSimulator(kernel, logger).isVisible = true
}
