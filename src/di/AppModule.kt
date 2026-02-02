package di

import abstraction.BinaryFormat
import abstraction.Content
import abstraction.DefaultBinaryFormat
import abstraction.DefaultFsConstants
import abstraction.DefaultPermissionUtils
import abstraction.DefaultSpaceManager
import abstraction.FsConstants
import abstraction.PermissionUtils
import abstraction.SpaceManager
import binary.Binary
import hardware.DefaultHardDisk
import hardware.HardDisk
import infra.ConsoleLogger
import infra.Logger
import main.MyKernel
import operatingSystem.Kernel
import operations.BatchOperation
import operations.CatOperation
import operations.CdOperation
import operations.ChmodOperation
import operations.CpOperation
import operations.CreateFileOperation
import operations.DefaultKernelContext
import operations.DumpOperation
import operations.InfoOperation
import operations.KernelContext
import operations.LsOperation
import operations.MkdirOperation
import operations.MvOperation
import operations.RmOperation
import operations.RmdirOperation
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule =
    module {
        singleOf(::ConsoleLogger) bind Logger::class
        singleOf(::DefaultFsConstants) bind FsConstants::class
        singleOf(::DefaultBinaryFormat) bind BinaryFormat::class
        singleOf(::DefaultHardDisk) bind HardDisk::class
        singleOf(::DefaultSpaceManager) bind SpaceManager::class
        singleOf(::DefaultPermissionUtils) bind PermissionUtils::class
        factoryOf(::Content)
        singleOf(::Binary)
        singleOf(::DefaultKernelContext) bind KernelContext::class
        singleOf(::LsOperation)
        singleOf(::MkdirOperation)
        singleOf(::CdOperation)
        singleOf(::RmdirOperation)
        singleOf(::CpOperation)
        singleOf(::MvOperation)
        singleOf(::RmOperation)
        singleOf(::ChmodOperation)
        singleOf(::CreateFileOperation)
        singleOf(::CatOperation)
        singleOf(::BatchOperation)
        singleOf(::DumpOperation)
        singleOf(::InfoOperation)
        singleOf(::MyKernel) bind Kernel::class
    }
