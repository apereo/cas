import java.util.*

Map<String, List<Object>> run(final Object... args) {
    def uid = args[0]
    def attributes = args[1]
    def logger = args[2]
    def casProperties = args[3]
    def casApplicationContext = args[4]
    def returnAttrs = [username:[uid],someattribute:["somevalue"], anotherattribute:["anothervalue"]]
    logger.warn("Groovy attributes retrieved for user [{}]", uid)
    return returnAttrs
}
