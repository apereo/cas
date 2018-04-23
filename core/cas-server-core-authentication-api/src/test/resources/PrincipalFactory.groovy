import org.apereo.cas.authentication.principal.*
import org.apereo.cas.authentication.*
import org.apereo.cas.util.*

def run(Object[] args) {
    def id = args[0]
    def attributes = args[1]
    def logger = args[2]
    return new SimplePrincipal(id, attributes)
}
