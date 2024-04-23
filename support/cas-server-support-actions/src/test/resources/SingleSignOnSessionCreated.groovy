import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.util.*
import org.apereo.cas.web.support.*
import org.springframework.webflow.*
import org.springframework.webflow.action.*

import jakarta.servlet.http.Cookie

def run(Object[] args) {
    def requestContext = args[0]
    def applicationContext = args[1]
    def properties = args[2]
    def logger = args[3]

    logger.info("Handling single signon created action...")
    def response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext)
    def cookie = new Cookie("type", "admin")
    cookie.setPath('/')
    cookie.setDomain('example.org')
    response.addCookie(cookie)
    return new EventFactorySupport().event(this, "success")
}
