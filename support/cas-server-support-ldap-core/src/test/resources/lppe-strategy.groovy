import org.apereo.cas.*
import java.util.*
import org.apereo.cas.authentication.*

def run(final Object... args) {
    def (response,configuration,logger,applicationContext) = args
    logger.debug("Handling password policy for [{}]", response)
    return [new DefaultMessageDescriptor("bad.authentication")]
}
