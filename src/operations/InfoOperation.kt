package operations

class InfoOperation(
    private val context: KernelContext,
) : Operation() {
    override fun execute(parameters: String): String {
        var result = ""
        context.log("System call: info")
        context.log("\tParameters: none")
        val name = "Fera da Silva"
        val registration = "2001.xx.yy.00.11"
        val version = "0.1"
        result += "Student name:      $name"
        result += "\nStudent ID:        $registration"
        result += "\nKernel version:    $version"
        return result
    }
}
