import org.apereo.cas.authentication.principal.*

def isWildcardAuthorized(Object... args) {
    def surrogate = args[0].toString()
    def principal = args[1] as Principal
    def service = args[2] as Service
    def logger = args[3]

    logger.info("Checking wildcard access {}", surrogate)
    return (surrogate == "anyone" || surrogate == "banderson") && principal.id == "casadmin"
}

def canAuthenticate(Object... args) {
    def surrogate = args[0].toString()
    def principal = args[1] as Principal
    def service = args[2] as Service
    def logger = args[3]

    logger.info("Checking surrogate access {}", surrogate)
    def accounts = getAccounts(principal.id, service, logger)
    return accounts.contains(surrogate)
}


def getAccounts(Object... args) {
    def user = args[0].toString()
    def service = args[1] as Service
    def logger = args[2]

    logger.info("Getting accounts for {}", user)
    switch (user) {
        case "casuser":
            return ["jsmith", "banderson"]
        case "casadmin":
            return ["*"]
        default:
            return []
    }
}
