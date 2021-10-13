import groovy.json.*

def run(Object[] args) {
    def principal = args[0]
    def userProfile = args[1]
    def client = args[2]
    def logger = args[3]

    logger.debug("Provisioning principal ${principal} via client ${client.name}")
    def destination = new File(System.getProperty("java.io.tmpdir"), "profile.txt")
    if (destination.exists()) {
        destination.delete()
    }
    logger.info("Destination file: ${destination}")
    def json = new JsonBuilder(principal).toPrettyString()
    destination.write(json)
    logger.info("Provisioning is complete.")
}
