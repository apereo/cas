package org.apereo.cas.audit.spi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationCredentialsLocalBinder;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.aspectj.lang.JoinPoint;


/**
 * Inspektr PrincipalResolver that gets the value for principal id from Authentication object bound to a current thread of execution.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
public class ThreadLocalPrincipalResolver implements PrincipalResolver {
    private final AuditPrincipalIdProvider auditPrincipalIdProvider;

    @Override
    public String resolveFrom(final JoinPoint auditTarget, final Object returnValue) {
        LOGGER.trace("Resolving principal at audit point [{}]", auditTarget);
        return getCurrentPrincipal(returnValue, null);
    }

    @Override
    public String resolveFrom(final JoinPoint auditTarget, final Exception exception) {
        LOGGER.trace("Resolving principal at audit point [{}] with thrown exception [{}]", auditTarget, exception);
        return getCurrentPrincipal(null, exception);
    }

    @Override
    public String resolve() {
        return UNKNOWN_USER;
    }

    private String getCurrentPrincipal(final Object returnValue, final Exception exception) {
        final Authentication authn = AuthenticationCredentialsLocalBinder.getCurrentAuthentication();
        String principal = this.auditPrincipalIdProvider.getPrincipalIdFrom(authn, returnValue, exception);
        if (principal == null) {
            principal = AuthenticationCredentialsLocalBinder.getCurrentCredentialIdsAsString();
        }
        return principal != null ? principal : UNKNOWN_USER;
    }
}
