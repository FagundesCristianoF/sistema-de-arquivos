package infra

class ConsoleLogger : Logger {
    override fun info(message: String) {
        kotlin.io.println(message)
    }

    override fun warn(message: String) {
        kotlin.io.println("WARN: $message")
    }

    override fun error(
        message: String,
        throwable: Throwable?,
    ) {
        System.err.println("ERROR: $message")
        throwable?.printStackTrace()
    }
}
