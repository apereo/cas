package org.apereo.cas.audit.spi.principal;

import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.audit.AuditableExecutionResult;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.principal.Principal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.spi.PrincipalResolver;
import org.aspectj.lang.JoinPoint;

import java.util.Optional;


/**
 * Inspektr PrincipalResolver that gets the value for principal id from Authentication object bound to a current thread of execution.
 *
 * @author Dmitriy Kopylenko
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class ThreadLocalAuditPrincipalResolver implements PrincipalResolver {
    private final AuditPrincipalIdProvider auditPrincipalIdProvider;

    @Override
    public String resolveFrom(final JoinPoint auditTarget, final Object returnValue) {
        LOGGER.trace("Resolving principal at audit point [{}]", auditTarget);
        return getCurrentPrincipal(auditTarget, returnValue, null);
    }

    @Override
    public String resolveFrom(final JoinPoint auditTarget, final Exception exception) {
        LOGGER.trace("Resolving principal at audit point [{}] with thrown exception [{}]", auditTarget, exception);
        return getCurrentPrincipal(auditTarget, null, exception);
    }

    @Override
    public String resolve() {
        return UNKNOWN_USER;
    }

    private String getCurrentPrincipal(final JoinPoint auditTarget, final Object returnValue, final Exception exception) {
        val authn = AuthenticationCredentialsThreadLocalBinder.getCurrentAuthentication();
        val principal = auditPrincipalIdProvider.getPrincipalIdFrom(auditTarget, authn, returnValue, exception);
        val id = Optional.ofNullable(principal)
            .or(() -> (returnValue instanceof AuditableExecutionResult result)
                ? result.getAuthentication().map(Authentication::getPrincipal).map(Principal::getId)
                : Optional.empty())
            .orElseGet(AuthenticationCredentialsThreadLocalBinder::getCurrentCredentialIdsAsString);
        return StringUtils.defaultIfBlank(id, UNKNOWN_USER);
    }
}
