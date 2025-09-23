import org.apereo.cas.api.*
import org.apereo.cas.util.model.*
import org.apereo.cas.api.*
import java.nio.charset.*

def run(Object[] args) {

    def account = args[0] as PasswordlessUserAccount
    def applicationContext = args[1]
    def logger = args[2]

    logger.info("Customizing $account")
    if (account.username == "caspassword") {
        account.setMultifactorAuthenticationEligible(TriStateBoolean.FALSE)
        account.setDelegatedAuthenticationEligible(TriStateBoolean.FALSE)
        account.setRequestPassword(true)
    }
    logger.info("Final account $account")
    return account
}
