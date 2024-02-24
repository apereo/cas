import org.apereo.cas.services.*
import org.apereo.cas.authentication.principal.*

def isServiceAccessAllowed(RegisteredService registeredService, Service service) {
    return service.id.endsWith("allowed")
}

def isServiceAccessAllowedForSso(RegisteredService registeredService) {
    return registeredService != null;
}

def authorizeRequest(RegisteredServiceAccessStrategyRequest request) {
    println "authorizing request for ${request.service}"
    def url = request.service.attributes["jakarta.servlet.http.HttpServletRequest.requestURL"][0] as String
    println "Request URL is ${url}"
    return url.endsWith("/logout")
}
