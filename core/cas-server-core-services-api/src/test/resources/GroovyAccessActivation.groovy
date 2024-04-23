import org.apereo.cas.services.*

def run(Object[] args) {
    def context = args[0] as RegisteredServiceAccessStrategyRequest
    def logger = args[1]
    logger.info("Checking access for ${context.registeredService.name}")
    return true
}
