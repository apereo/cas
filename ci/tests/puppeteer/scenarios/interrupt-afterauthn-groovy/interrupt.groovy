import org.apereo.cas.interrupt.*

def run(final Object... args) {
    def principal = args[0]
    def attributes = args[1]
    def service = args[2]
    def registeredService = args[3]
    def requestContext = args[4]
    def logger = args[5]

    logger.info("Constructing interrupt response for [{}]", principal)
    if (principal.id == "blockuser") {
        logger.warn("Blocking user [{}]", principal)
        return new InterruptResponse("Blocked!", [link1:"https://localhost:9859/anything/cas"], true, false)
    }
    return new InterruptResponse("Hello World!", [link1:"https://google.com", link2:"https://yahoo.com"], false, true)
}
