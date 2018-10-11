package org.apereo.cas.audit.spi.principal;

import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.aspectj.lang.JoinPoint;


/**
 * Inspektr PrincipalResolver that gets the value for principal id from Authentication object bound to a current thread of execution.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
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
        val authn = AuthenticationCredentialsThreadLocalBinder.getCurrentAuthentication();
        val principal = this.auditPrincipalIdProvider.getPrincipalIdFrom(authn, returnValue, exception);
        val id = FunctionUtils.doIfNull(principal,
            AuthenticationCredentialsThreadLocalBinder::getCurrentCredentialIdsAsString,
            () -> principal)
            .get();

        return StringUtils.defaultIfBlank(id, UNKNOWN_USER);
    }
}
