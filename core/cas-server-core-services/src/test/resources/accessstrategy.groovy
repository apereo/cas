import org.apereo.cas.services.*
import java.util.*

class GroovyRegisteredAccessStrategy extends DefaultRegisteredServiceAccessStrategy {
    @Override
    boolean isServiceAccessAllowed() {
        super.isServiceAccessAllowed()
    }

    @Override
    boolean isServiceAccessAllowedForSso() {
        super.isServiceAccessAllowedForSso()
    }

    @Override
    boolean doPrincipalAttributesAllowServiceAccess(String principal, Map<String, Object> attributes) {
        super.doPrincipalAttributesAllowServiceAccess(principal, attributes)
    }
}
