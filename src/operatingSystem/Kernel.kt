package operatingSystem

/**
 * Interface that defines system calls for file system management.
 */
interface Kernel {
    /**
     * List directories and files.
     *
     * @param parameters Parameters received from the terminal.
     * @return String printed to the terminal.
     */
    fun ls(parameters: String): String

    /**
     * Create directory.
     *
     * @param parameters Parameters received from the terminal.
     * @return String printed to the terminal.
     */
    fun mkdir(parameters: String): String

    /**
     * Navigate between directories.
     *
     * @param parameters Parameters received from the terminal.
     * @return String printed to the terminal.
     */
    fun cd(parameters: String): String

    /**
     * Remove an empty directory.
     *
     * @param parameters Parameters received from the terminal.
     * @return String printed to the terminal.
     */
    fun rmdir(parameters: String): String

    /**
     * Copy files and directories.
     *
     * @param parameters Parameters received from the terminal.
     * @return String printed to the terminal.
     */
    fun cp(parameters: String): String

    /**
     * Move files and directories.
     *
     * @param parameters Parameters received from the terminal.
     * @return String printed to the terminal.
     */
    fun mv(parameters: String): String

    /**
     * Remove files and directories.
     *
     * @param parameters Parameters received from the terminal.
     * @return String printed to the terminal.
     */
    fun rm(parameters: String): String

    /**
     * Define file and directory permissions.
     *
     * @param parameters Parameters received from the terminal.
     * @return String printed to the terminal.
     */
    fun chmod(parameters: String): String

    /**
     * Create files.
     *
     * @param parameters Parameters received from the terminal.
     * @return String printed to the terminal.
     */
    fun createfile(parameters: String): String

    /**
     * Show file contents.
     *
     * @param parameters Parameters received from the terminal.
     * @return String printed to the terminal.
     */
    fun cat(parameters: String): String

    /**
     * Run a batch file.
     *
     * @param parameters Parameters received from the terminal.
     * @return String printed to the terminal.
     */
    fun batch(parameters: String): String

    /**
     * Generate a file system dump.
     *
     * @param parameters Parameters received from the terminal.
     * @return String printed to the terminal.
     */
    fun dump(parameters: String): String

    /**
     * Provide simulator information.
     *
     * @return String printed to the terminal.
     */
    fun info(): String
}
