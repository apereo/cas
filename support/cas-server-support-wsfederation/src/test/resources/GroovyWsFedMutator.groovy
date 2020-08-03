import org.apereo.cas.*
import java.util.*
import org.apereo.cas.authentication.*

def Map run(final Object... args) {
    def attributes = args[0]
    def logger = args[1]
    logger.info("Mutating attributes {}", attributes)
    return [mail: ["cas@apereo.org"]]
}
