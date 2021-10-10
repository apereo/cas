package org.apereo.cas.acct;

import org.apereo.cas.audit.AuditPrincipalIdProvider;
import org.apereo.cas.authentication.Authentication;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.aspectj.lang.JoinPoint;
import org.springframework.core.Ordered;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * This is {@link AccountRegistrationRequestAuditPrincipalIdResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Getter
@Setter
@RequiredArgsConstructor
public class AccountRegistrationRequestAuditPrincipalIdResolver implements AuditPrincipalIdProvider {
    private final AccountRegistrationService accountRegistrationService;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public String getPrincipalIdFrom(final JoinPoint auditTarget, final Authentication authentication,
                                     final Object resultValue, final Exception exception) {
        if (resultValue instanceof AccountRegistrationRequest) {
            return ((AccountRegistrationRequest) resultValue).getUsername();
        }
        val context = RequestContextHolder.getRequestContext();
        return AccountRegistrationUtils.getAccountRegistrationRequestUsername(context);
    }

    @Override
    public boolean supports(final JoinPoint auditTarget, final Authentication authentication,
                            final Object resultValue, final Exception exception) {
        val context = RequestContextHolder.getRequestContext();
        return resultValue instanceof AccountRegistrationRequest
            || (context != null && AccountRegistrationUtils.getAccountRegistrationRequest(context) != null);
    }
}
