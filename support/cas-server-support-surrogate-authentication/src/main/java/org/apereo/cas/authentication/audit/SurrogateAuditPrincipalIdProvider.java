package org.apereo.cas.authentication.audit;

import org.apereo.cas.audit.spi.principal.DefaultAuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;

import lombok.val;
import org.aspectj.lang.JoinPoint;

/**
 * This is {@link SurrogateAuditPrincipalIdProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class SurrogateAuditPrincipalIdProvider extends DefaultAuditPrincipalIdProvider {

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE - 1;
    }

    @Override
    public String getPrincipalIdFrom(final JoinPoint auditTarget, final Authentication authentication,
                                     final Object returnValue, final Exception exception) {
        if (authentication == null) {
            return Credential.UNKNOWN_ID;
        }
        if (supports(auditTarget, authentication, returnValue, exception)) {
            val attributes = authentication.getAttributes();
            val surrogateUser = attributes.get(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER).get(0).toString();
            val principalId = attributes.get(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL).get(0).toString();
            return String.format("(Primary User: [%s], Surrogate User: [%s])", principalId, surrogateUser);
        }
        return super.getPrincipalIdFrom(auditTarget, authentication, returnValue, exception);
    }

    @Override
    public boolean supports(final JoinPoint auditTarget, final Authentication authentication,
                            final Object resultValue, final Exception exception) {
        return super.supports(auditTarget, authentication, resultValue, exception)
            && authentication.getAttributes().containsKey(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER);
    }
}
