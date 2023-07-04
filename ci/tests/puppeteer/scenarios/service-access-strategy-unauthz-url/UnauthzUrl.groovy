import org.apereo.cas.*
import org.apereo.cas.web.support.*
import java.util.*
import java.net.*
import org.apereo.cas.authentication.*

URI run(final Object... args) {
    def registeredService = args[0]
    def authentication = args[1] 
    def requestContext = args[2]
    def applicationContext = args[3]
    def logger = args[4]
    logger.info("Building unauthorized redirect URI for service [{}]", registeredService.name)
    def segment = authentication.principal.attributes["segment"][0] as String
    def url = "https://localhost:9859/anything/${segment}"
    logger.info("Redirecting to ${url}")
    return new URI(url);
}
