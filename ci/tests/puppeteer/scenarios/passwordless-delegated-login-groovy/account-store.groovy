import org.apereo.cas.api.*
import org.apereo.cas.util.model.*
import java.nio.charset.*
import groovy.json.*

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
    def username = args[0]
    def logger = args[1]

    def endpoint = "http://localhost:5432/${username}.json"
    logger.info("Locating user record for user $username @ $endpoint")
    def text = new URL(endpoint).text
    
    def slurper = new JsonSlurper()
    logger.info("Parsing results for $username")
    def results = (List<Payload>) slurper.parseText(text)
    logger.info("Results: {}", results)
    
    if (results.isEmpty()) {
        return null
    }

    def entry = results[0]
    if (results.size() == 1 && entry.authType == "none") {
        logger.info("Could not locate user record for $username")
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
    account.email = "$username@example.org"

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
