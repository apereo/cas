import org.apereo.cas.interrupt.*
import org.apereo.cas.services.*

def run(final Object... args) {
    def principal = args[0]
    def attributes = args[1]
    def service = args[2]
    def registeredService = args[3] as RegisteredService
    def requestContext = args[4]
    def logger = args[5]

    logger.debug("Constructing interrupt response for [{}]", principal)
    def group = registeredService.properties.get("group").value()
    return new InterruptResponse(true).setMessageCode("interrupt.group.${group}")
}
