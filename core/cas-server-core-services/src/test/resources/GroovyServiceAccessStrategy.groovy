import org.apereo.cas.services.*

def isServiceAccessAllowed() {
    true
}

def isServiceAccessAllowedForSso() {
    true
}

def authorizeRequest(RegisteredServiceAccessStrategyRequest request) {
    request.service != null
}
