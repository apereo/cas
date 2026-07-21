def collect(Object... args) {
    def (claims, name, values, registeredService, applicationContext, logger) = args
    claims.setStringClaim("cn", "CAS User")
}

def conclude(Object... args) {
    def (claims, registeredService, applicationContext, logger) = args
    claims.setStringClaim("givenName", "ApereoCAS")
}
