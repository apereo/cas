import org.apereo.cas.authentication.principal.*
import org.apereo.cas.web.*
import org.apereo.cas.util.*
import org.pac4j.core.profile.*
import org.springframework.webflow.execution.*
import java.util.*

def run(Object[] args) {
    def requestContext = args[0] as RequestContext
    def clientCredentials = args[1] as ClientCredential
    def userProfile = (args[2] as Optional<UserProfile>).get()
    def logger = args[3]

    logger.info("Checking ${userProfile.id}...")
    def profile1 = DelegatedAuthenticationCandidateProfile.builder()
            .attributes(userProfile.getAttributes())
            .id(RandomUtils.randomAlphanumeric(6))
            .key(UUID.randomUUID().toString())
            .linkedId(userProfile.id)
            .build()
    def profile2 = DelegatedAuthenticationCandidateProfile.builder()
            .attributes(userProfile.getAttributes())
            .id(RandomUtils.randomAlphanumeric(6))
            .key(UUID.randomUUID().toString())
            .linkedId(userProfile.id)
            .build()
    return [profile1, profile2]
}
