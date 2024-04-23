import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.util.*
import org.springframework.webflow.*
import org.springframework.webflow.action.*

def run(Object[] args) {
    def requestContext = args[0]
    def applicationContext = args[1]
    def properties = args[2]
    def logger = args[3]

    logger.info("Handling action...")
    return new EventFactorySupport().event(this, "result")
}
