package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorAuthenticationPrepareTrustDeviceViewAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class MultifactorAuthenticationPrepareTrustDeviceViewAction extends AbstractAction {

    private final MultifactorAuthenticationTrustStorage storage;

    private final DeviceFingerprintStrategy deviceFingerprintStrategy;

    private final TrustedDevicesMultifactorProperties trustedProperties;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final ServicesManager servicesManager;

    private final MultifactorAuthenticationTrustedDeviceBypassEvaluator bypassEvaluator;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val authn = WebUtils.getAuthentication(requestContext);
        val registeredService = WebUtils.getRegisteredService(requestContext);
        val service = WebUtils.getService(requestContext);
        if (bypassEvaluator.shouldBypassTrustedDevice(registeredService, service, authn)) {
            LOGGER.debug("Trusted device registration is disabled for [{}]", registeredService);
            return result(CasWebflowConstants.TRANSITION_ID_SKIP);
        }
        return result(CasWebflowConstants.TRANSITION_ID_REGISTER);
    }
}
