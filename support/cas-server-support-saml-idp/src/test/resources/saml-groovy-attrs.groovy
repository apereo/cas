import java.util.*
import org.apereo.cas.support.saml.services.*
import org.apereo.cas.support.saml.*

def Map<String, Object> run(final Object... args) {
    def attributes = args[0]
    def service = args[1]
    def resolver = args[2]
    def facade = args[3]
    def entityDescriptor = args[4]
    def applicationContext = args[5]
    def logger = args[6]

    logger.debug("Fetching attributes for {}", entityDescriptor.entityID)
    return [uid: "casuser", displayName: attributes["displayName"], "givenName": attributes["givenName"]]
}
