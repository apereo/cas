import org.apereo.cas.authentication.principal.*
import org.apereo.cas.services.*

def isServiceAccessAllowed(RegisteredService registeredService, Service service) {
    if (registeredService == null) {
        throw new RuntimeException("Failed")
    }
    registeredService != null
}

def isServiceAccessAllowedForSso(RegisteredService registeredService) {
    if (registeredService == null) {
        throw new RuntimeException("Failed")
    }
    registeredService != null
}

def authorizeRequest(RegisteredServiceAccessStrategyRequest request) {
    if (request == null) {
        throw new RuntimeException("Failed")
    }
    request.service != null
}
