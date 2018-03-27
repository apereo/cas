import org.apereo.cas.*
import java.util.*
import org.apereo.cas.authentication.*

def List<MessageDescriptor> run(final Object... args) {
    def response = args[0]
    def configuration = args[1];
    def logger = args[2]
    logger.debug("Handling password policy for [{}]", response)
    return [new DefaultMessageDescriptor("bad.authentication")]
}
