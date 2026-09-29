import org.apereo.cas.authentication.principal.*
import org.apereo.cas.services.*
import groovy.transform.*
import org.slf4j.*

@Field def logger = LoggerFactory.getLogger("GroovyServiceAccessStrategy")

def isServiceAccessAllowed(RegisteredService registeredService, Service service) {
    if (registeredService == null) {
        throw new RuntimeException("Failed")
    }
    logger.info("Checking access for service [{}] and registered service [{}]", service, registeredService)
    registeredService != null
}

def isServiceAccessAllowedForSso(RegisteredService registeredService) {
    if (registeredService == null) {
        throw new RuntimeException("Failed")
    }
    logger.info("Checking SSO access for registered service [{}]", registeredService)
    registeredService != null
}

def authorizeRequest(RegisteredServiceAccessStrategyRequest request) {
    if (request == null) {
        throw new RuntimeException("Failed")
    }
    logger.info("Principal [{}] with attributes [{}] => Checking service [{}], registeredService [{}]",
            request.principalId, request.attributes,
            request.service, request.registeredService)
    request.service != null
}
