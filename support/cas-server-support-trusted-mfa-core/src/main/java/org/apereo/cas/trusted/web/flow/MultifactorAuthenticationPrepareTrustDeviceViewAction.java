package org.apereo.cas.trusted.web.flow;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceBypassEvaluator;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceNamingStrategy;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
public class MultifactorAuthenticationPrepareTrustDeviceViewAction extends BaseCasWebflowAction {

    private final MultifactorAuthenticationTrustStorage storage;

    private final DeviceFingerprintStrategy deviceFingerprintStrategy;

    private final TrustedDevicesMultifactorProperties trustedProperties;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final ServicesManager servicesManager;

    private final MultifactorAuthenticationTrustedDeviceBypassEvaluator bypassEvaluator;

    private final MultifactorAuthenticationTrustedDeviceNamingStrategy namingStrategy;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        val authn = WebUtils.getAuthentication(requestContext);
        val registeredService = WebUtils.getRegisteredService(requestContext);
        val service = WebUtils.getService(requestContext);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);

        if (!storage.isAvailable() || bypassEvaluator.shouldBypassTrustedDevice(registeredService, service, authn)) {
            LOGGER.debug("Trusted device registration store is unavailable or is disabled for [{}]", registeredService);
            return result(CasWebflowConstants.TRANSITION_ID_SKIP);
        }
        if (trustedProperties.getCore().isAutoAssignDeviceName()) {
            WebUtils.getMultifactorAuthenticationTrustRecord(requestContext, MultifactorAuthenticationTrustBean.class)
                .ifPresent(device -> {
                    val deviceName = namingStrategy.determineDeviceName(registeredService, service, request, authn);
                    LOGGER.debug("Auto-generated device name is [{}]", deviceName);
                    device.setDeviceName(deviceName);
                });
            return result(CasWebflowConstants.TRANSITION_ID_STORE);
        }

        return result(CasWebflowConstants.TRANSITION_ID_REGISTER);
    }
}
