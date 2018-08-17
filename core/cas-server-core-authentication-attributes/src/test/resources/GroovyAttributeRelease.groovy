import java.util.*

def Map<String, List<Object>> run(final Object... args) {
    def currentAttributes = args[0]
    def logger = args[1]
    def principal = args[2]
    def service = args[3]

    logger.debug("Current attributes received are {}", currentAttributes)
    return [username:["something"], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
}
