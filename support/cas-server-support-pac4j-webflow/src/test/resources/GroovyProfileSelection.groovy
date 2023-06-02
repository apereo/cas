import org.apereo.cas.authentication.principal.*
import org.apereo.cas.web.*
import org.pac4j.core.profile.*
import org.springframework.webflow.execution.*
import java.util.*

def run(Object[] args) {
    def requestContext = args[0] as RequestContext
    def clientCredentials = args[1] as ClientCredential
    def userProfile = (args[2] as Optional<UserProfile>).get()
    def logger = args[3]

    logger.info("Checking ${userProfile.id}...")
    def profile = DelegatedAuthenticationCandidateProfile.builder()
            .attributes(userProfile.getAttributes())
            .id("resolved-casuser")
            .key("1234567890")
            .linkedId(userProfile.id)
            .build()
    return [profile]
}
