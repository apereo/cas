import org.apereo.cas.*
import org.apereo.cas.web.support.*
import java.util.*
import java.net.*
import org.apereo.cas.authentication.*

def run(final Object... args) {
    def (registeredService,authentication,requestContext,applicationContext,logger) = args
    logger.info("Building unauthorized redirect URI for service [{}]", registeredService.name)
    def segment = authentication.principal.attributes["segment"][0] as String
    def url = "https://localhost:9859/anything/${segment}"
    logger.info("Redirecting to ${url}")
    new URI(url)
}
