package org.apereo.cas.trusted.web.flow;

import module java.base;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustedDeviceNamingStrategy;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintStrategy;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.actions.composite.MultifactorProviderSelectionCriteria;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link MultifactorAuthenticationTrustProviderSelectionCriteria}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class MultifactorAuthenticationTrustProviderSelectionCriteria implements MultifactorProviderSelectionCriteria {

    protected final ServicesManager servicesManager;

    protected final MultifactorAuthenticationTrustStorage mfaTrustEngine;

    protected final MultifactorAuthenticationTrustedDeviceNamingStrategy mfaTrustDeviceNamingStrategy;

    protected final DeviceFingerprintStrategy deviceFingerprintStrategy;

    protected final CasCookieBuilder deviceFingerprintCookieGenerator;

    protected final CasConfigurationProperties casProperties;

    @Override
    public boolean shouldProceedWithMultifactorProviderSelection(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val principal = authentication.getPrincipal().getId();

        LOGGER.trace("Retrieving trusted authentication records for [{}]", principal);
        val results = mfaTrustEngine.get(principal);
        
        val fingerprint = deviceFingerprintStrategy.determineFingerprint(authentication, request, response);
        LOGGER.trace("Checking trusted authentication records for [{}] that matches [{}]", principal, fingerprint);

        val trustedDevices = results.stream().filter(entry -> entry.getDeviceFingerprint().equals(fingerprint)).toList();
        if (trustedDevices.isEmpty()) {
            LOGGER.debug("No trusted authentication records could be found for [{}] to match the current device fingerprint", principal);
            return true;
        }
        val provider = requestContext.getCurrentEvent().getAttributes()
            .get(MultifactorAuthenticationProvider.class.getName(), MultifactorAuthenticationProvider.class);
        val matchingDevice = trustedDevices
            .stream()
            .filter(device -> StringUtils.isNotBlank(device.getMultifactorAuthenticationProvider()))
            .filter(device -> provider.matches(device.getMultifactorAuthenticationProvider()))
            .findFirst();
        if (matchingDevice.isPresent()) {
            val mfaProvider = MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(
                matchingDevice.get().getMultifactorAuthenticationProvider(), requestContext.getActiveFlow().getApplicationContext());
            requestContext.getFlashScope().put("mfaProvider", mfaProvider.orElseThrow());
            return false;
        }
        return true;
    }
}
