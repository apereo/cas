import org.apereo.cas.api.*

def run(Object[] args) {
    def passwordlessUser = args[0] as PasswordlessUserAccount
    def clients = args[1] as Set
    def httpServletRequest = args[2]
    def logger = args[3]

    logger.info("Available identity providers are {}", clients)
    logger.info("Passwordless user account is {}", passwordlessUser.username)
    if (passwordlessUser.username == "casuser-server") {
        return clients.find { it.name == "CasClient-Server" }
    }
    if (passwordlessUser.username == "casuser-client") {
        return clients.find { it.name == "CasClient-Client" }
    }
    if (passwordlessUser.username == "casuser-saml") {
        return clients.find { it.name == "SAML2Client" }
    }
    return clients.find { it.name == "CasClient-None" }
}
