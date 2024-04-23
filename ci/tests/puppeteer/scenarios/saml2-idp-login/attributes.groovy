import java.util.*

def run(final Object... args) {
    def uid = args[0]
    def attributes = args[1]
    def logger = args[2]
    def casProperties = args[3]
    def casApplicationContext = args[4]
    logger.info("Current attributes [{}]", attributes)
    def returnAttrs =
            [username:[uid], sciptedGivenNames: attributes["people"][0].attributes["givenName"]]
    logger.info("Groovy attributes retrieved for user: [{}]", returnAttrs)
    return returnAttrs
}
