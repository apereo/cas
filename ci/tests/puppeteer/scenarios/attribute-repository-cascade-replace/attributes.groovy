import java.util.*

def run(final Object... args) {
    def uid = args[0]
    def attributes = args[1]
    def logger = args[2]
    def properties = args[3]
    def applicationContext = args[4]

    logger.info("Current attributes [{}]", attributes)
    def returnAttrs =
        [
            username:[uid],
            lastname: attributes["prefix"][0] + attributes["lastname"][0],
            firstname: attributes["prefix"][0] + attributes["firstname"][0]
        ]
    logger.info("Groovy attributes retrieved for user: [{}]", returnAttrs)
    return returnAttrs
}
