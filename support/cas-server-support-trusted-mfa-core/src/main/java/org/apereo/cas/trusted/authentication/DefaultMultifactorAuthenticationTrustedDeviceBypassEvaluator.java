package org.apereo.cas.trusted.authentication;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link DefaultMultifactorAuthenticationTrustedDeviceBypassEvaluator}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class DefaultMultifactorAuthenticationTrustedDeviceBypassEvaluator implements MultifactorAuthenticationTrustedDeviceBypassEvaluator {
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    @Override
    public boolean shouldBypassTrustedDevice(final RegisteredService registeredService,
                                             final Service service,
                                             final Authentication authentication) {
        if (registeredService == null && service == null) {
            return false;
        }

        val audit = AuditableContext.builder()
            .service(service)
            .authentication(authentication)
            .registeredService(registeredService)
            .retrievePrincipalAttributesFromReleasePolicy(Boolean.FALSE)
            .build();
        val accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
        accessResult.throwExceptionIfNeeded();

        val mfaPolicy = registeredService != null ? registeredService.getMultifactorPolicy() : null;
        return mfaPolicy != null && mfaPolicy.isBypassTrustedDeviceEnabled();
    }
}
