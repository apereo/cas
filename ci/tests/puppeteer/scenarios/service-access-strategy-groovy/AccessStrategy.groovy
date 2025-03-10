def isServiceAccessAllowed(RegisteredService registeredService, Service service) {
    return service.id.contains("/allowed")
}

def isServiceAccessAllowedForSso(RegisteredService registeredService) {
    return registeredService != null
}

def authorizeRequest(RegisteredServiceAccessStrategyRequest request) {
    println "authorizing request for ${request.service.id}"
    def url = request.service.attributes["jakarta.servlet.http.HttpServletRequest.requestURL"][0] as String
    println "Request URL is ${url}"
    return url.endsWith("/logout")
}
