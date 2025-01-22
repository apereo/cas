import org.apereo.cas.interrupt.*

def run(final Object... args) {
    def principal = args[0]
    def attributes = args[1]
    def service = args[2]
    def registeredService = args[3]
    def requestContext = args[4]
    def logger = args[5]
    
    logger.info("Constructing interrupt response for [{}] and service [{}]", principal, service.id)
    def block = false
    def ssoEnabled = true

    def randomValue = UUID.randomUUID().toString();
    def links = [Continue: "https://localhost:8443/cas/login?service=https://localhost:9859/anything/external/" + randomValue]
    logger.info("Interrupt links are [{}]", links)
    return new InterruptResponse("Interrupted", links, block, ssoEnabled)
}
