import org.apereo.cas.api.*
import org.apereo.cas.util.model.*
import org.apereo.cas.api.*
import java.nio.charset.*

def run(Object[] args) {
    def passwordlessUser = args[0] as PasswordlessUserAccount
    def clients = (Set) args[1]
    def httpServletRequest = args[2]
    def logger = args[3]
    logger.info("Choosing external identity provider from {} for {}", clients, passwordlessUser)

    if (passwordlessUser.username == "casuser") {
        return clients.find({ c ->
            c.name == "SAML2Client"
        })
    }
    return null
}
