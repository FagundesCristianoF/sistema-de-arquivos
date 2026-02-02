package operations

class CpOperation(
    private val context: KernelContext,
) : Operation() {
    override fun execute(parameters: String): String {
        val result = ""
        context.log("System call: cp")
        context.log("\tParameters: $parameters")
        return result
    }
}
