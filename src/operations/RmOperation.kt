package operations

class RmOperation(
    private val context: KernelContext,
    private val rmdirOperation: RmdirOperation,
) : Operation() {
    override fun execute(parameters: String): String {
        val result = ""
        context.log("System call: rm")
        context.log("\tParameters: $parameters")
        val aux = parameters.replace("-r", "")
        val aux2 = aux.split("/")
        rmdirOperation.execute(aux)
        if (aux2.size == 1) {
            rmdirOperation.execute(parameters.replace("-r", ""))
        }
        return result
    }
}
