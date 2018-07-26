import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.util.*
import org.apereo.cas.authentication.adaptive.intel.*

def run(Object[] args) {
    def requestContext = args[0]
    def clientIpAddress = args[1]
    def logger = args[2]
    logger.info("Client ip address set to ${clientIpAddress}")
    return IPAddressIntelligenceResponse.banned()
}
