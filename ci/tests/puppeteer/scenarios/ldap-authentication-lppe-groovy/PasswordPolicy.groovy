import org.apereo.cas.*
import java.util.*
import org.apereo.cas.authentication.*

def run(final Object... args) {
    def response = args[0]
    def configuration = args[1]
    def logger = args[2]
    def applicationContext = args[3]
    logger.debug("Handling password policy for [{}]", response)
    return [new DefaultMessageDescriptor("password-policy-warning")]
}
