class Payload {
    String accountId
    String accountName
    String authType
    String id
    String mfaType
    String serviceId
    String userName
}

def run(Object[] args) {
    def request = args[0] as PasswordlessAuthenticationRequest
    def logger = args[1]

    def endpoint = "http://localhost:5432/${request.username}.json"
    logger.info("Locating user record for user $request.username @ $endpoint")
    def text = new URL(endpoint).text
    
    def slurper = new JsonSlurper()
    logger.info("Parsing results for $request.username")
    def results = (List<Payload>) slurper.parseText(text)
    logger.info("Results: {}", results)
    
    if (results.isEmpty()) {
        return null
    }

    def entry = results[0]
    if (results.size() == 1 && entry.authType == "none") {
        logger.info("Could not locate user record for $request.username")
        return null
    }

    def account = new PasswordlessUserAccount()
    /**
     * We assume certain fields of the user account
     * are the same for all entries, i.e. accountId, userName, etc.
     */
    account.username = entry.userName
    def attributes = [
        accountName: [entry.accountName],
        accountId: [entry.accountId],
        id: [entry.id],
        mfaType: [entry.mfaType],
        serviceId: [entry.serviceId],
        authType: [entry.authType],
    ]
    account.attributes = attributes

    account.setMultifactorAuthenticationEligible(TriStateBoolean.FALSE)
    account.setDelegatedAuthenticationEligible(TriStateBoolean.FALSE)
    account.setRequestPassword(false)
    account.email = "$request.username@example.org"

    results.each {rec ->
        if (rec.authType == "local") {
            account.setRequestPassword(true)
        } else {
            account.setDelegatedAuthenticationEligible(TriStateBoolean.TRUE)
            account.allowedDelegatedClients += rec.authType;
        }
    }

    logger.info("Final user account: {}", account)
    return account
}
