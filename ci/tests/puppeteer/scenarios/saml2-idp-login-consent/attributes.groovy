import java.util.*
import org.apereo.cas.support.saml.services.*
import org.apereo.cas.support.saml.*

Map<String, Object> run(final Object... args) {
    def attributes = args[0] as Map<String, Object>
    def service = args[1]
    def resolver = args[2]
    def facade = args[3]
    def entityDescriptor = args[4]
    def applicationContext = args[5]
    def logger = args[6]

    logger.info("Releasing attributes {} for {}", attributes, entityDescriptor.entityID)
    return attributes
}
