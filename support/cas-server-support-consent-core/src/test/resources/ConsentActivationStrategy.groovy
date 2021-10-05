import org.apereo.cas.util.model.TriStateBoolean

def run(Object[] args) {
    def consentEngine = args[0]
    def casProperties = args[1]
    def service = args[2]
    def registeredService = args[3]
    def authentication = args[4]
    def request = args[5]
    def logger = args[6]

    logger.debug("Activating consent for ${service}")
    return registeredService.attributeReleasePolicy.consentPolicy.status != TriStateBoolean.FALSE
}
