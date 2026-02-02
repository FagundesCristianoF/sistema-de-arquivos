package operations

class MvOperation(
    private val context: KernelContext,
) : Operation() {
    override fun execute(parameters: String): String {
        val result = ""
        context.log("System call: mv")
        context.log("\tParameters: $parameters")
        return result
    }
}
