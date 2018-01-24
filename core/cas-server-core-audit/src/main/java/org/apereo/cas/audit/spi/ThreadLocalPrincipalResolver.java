package org.apereo.cas.audit.spi;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditPrincipalIdProvider;
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
        return getCurrentPrincipal();
    }

    @Override
    public String resolveFrom(final JoinPoint auditTarget, final Exception exception) {
        LOGGER.trace("Resolving principal at audit point [{}] with thrown exception [{}]", auditTarget, exception);
        return getCurrentPrincipal();
    }

    @Override
    public String resolve() {
        return UNKNOWN_USER;
    }

    private String getCurrentPrincipal() {
        String principal = this.auditPrincipalIdProvider.getPrincipalIdFrom(AuthenticationCredentialsLocalBinder.getCurrentAuthentication());
        if (principal == null) {
            principal = AuthenticationCredentialsLocalBinder.getCurrentCredentialIdsAsString();
        }
        return principal != null ? principal : UNKNOWN_USER;
    }
}
