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
            username: [uid],
            color: ["Yellow"],
            department: ["IAM"]
        ]
    logger.info("Groovy attributes retrieved for user: [{}]", returnAttrs)
    return returnAttrs
}
