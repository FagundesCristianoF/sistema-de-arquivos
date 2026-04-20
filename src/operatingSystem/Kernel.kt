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
