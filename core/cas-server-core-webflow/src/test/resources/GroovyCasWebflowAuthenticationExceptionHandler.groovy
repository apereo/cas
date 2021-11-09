import org.apereo.cas.*
import org.springframework.context.*
import org.springframework.webflow.*
import org.springframework.webflow.action.*
import org.springframework.webflow.execution.*

def run(Object[] args) {
    def exception = args[0] as Exception
    def requestContext = args[1] as RequestContext
    def applicationContext = args[2] as ApplicationContext
    def logger = args[3]

    logger.info("Handling {}", exception)
    new EventFactorySupport().event(this, "customEvent")
}

def supports(Object[] args) {
    def exception = args[0] as Exception
    def requestContext = args[1] as RequestContext
    def applicationContext = args[2] as ApplicationContext
    def logger = args[3]

    logger.info("Checking to support {}", exception)
    true
}
