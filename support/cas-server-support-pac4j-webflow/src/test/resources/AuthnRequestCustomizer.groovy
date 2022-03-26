import org.apereo.cas.authentication.principal.*
import org.apereo.cas.web.*
import org.apereo.cas.web.support.*
import org.pac4j.core.client.*
import org.pac4j.core.context.*
import org.springframework.context.*

def run(Object[] args) {
    def client = args[0] as IndirectClient
    def webContext = args[1] as WebContext
    def appContext = args[2] as ApplicationContext
    def logger = args[3]
    logger.info("Checking ${client.name}...")
    webContext.setRequestAttribute("customAttribute", "value")
}

def supports(Object[] args) {
    def client = args[0] as IndirectClient
    def webContext = args[1] as WebContext
    def appContext = args[2] as ApplicationContext
    def logger = args[3]
    logger.info("Checking support for ${client.name}...")
    return true
}

def isAuthorized(Object[] args) {
    def client = args[0] as IndirectClient
    def webContext = args[1] as WebContext
    def service = args[2] as WebApplicationService
    def appContext = args[3] as ApplicationContext
    def logger = args[4]
    logger.info("Checking authorization for ${client.name}...")
    return true
}
