package org.apereo.cas.authentication.audit;

import module java.base;
import org.apereo.cas.audit.spi.principal.DefaultAuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.jspecify.annotations.Nullable;

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
    public @Nullable String getPrincipalIdFrom(final JoinPoint auditTarget,
                                               @Nullable final Authentication authentication,
                                               @Nullable final Object returnValue,
                                               @Nullable final Exception exception) {
        if (authentication == null) {
            return Credential.UNKNOWN_ID;
        }
        if (supports(auditTarget, authentication, returnValue, exception)) {
            val attributes = authentication.getAttributes();
            val surrogateUser = Objects.requireNonNull(attributes.get(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER))
                .getFirst().toString();
            val principalId = Objects.requireNonNull(attributes.get(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_PRINCIPAL))
                .getFirst().toString();
            return String.format("(Primary User: [%s], Surrogate User: [%s])", principalId, surrogateUser);
        }
        return super.getPrincipalIdFrom(auditTarget, authentication, returnValue, exception);
    }

    @Override
    public boolean supports(final JoinPoint auditTarget, @Nullable final Authentication authentication,
                            @Nullable final Object resultValue, @Nullable final Exception exception) {
        return super.supports(auditTarget, authentication, resultValue, exception)
            && authentication != null
            && authentication.containsAttribute(SurrogateAuthenticationService.AUTHENTICATION_ATTR_SURROGATE_USER);
    }
}
