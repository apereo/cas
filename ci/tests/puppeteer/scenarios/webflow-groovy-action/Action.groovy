import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.util.*
import org.apereo.cas.web.support.*
import org.springframework.webflow.*
import org.springframework.webflow.action.*
import org.apereo.cas.authentication.*
import org.apereo.cas.authentication.principal.*

import jakarta.servlet.http.Cookie

def run(Object[] args) {
    def requestContext = args[0]
    def applicationContext = args[1]
    def properties = args[2]
    def logger = args[3]

    def response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext)
    def authentication = WebUtils.getAuthentication(requestContext) as Authentication
    if (authentication != null) {
        def principal = authentication.getPrincipal() as Principal
        logger.info("Handling single signon created action for ${principal.id}...",)

        def cookie = new Cookie("CASWebflowCookie", principal.id)
        cookie.setPath('/cas')
        response.addCookie(cookie)
        logger.info("Added cookie ${cookie.name}...")
    } else {
        logger.error("Unable to locate authentication in the webflow")
    }
    return null
}
