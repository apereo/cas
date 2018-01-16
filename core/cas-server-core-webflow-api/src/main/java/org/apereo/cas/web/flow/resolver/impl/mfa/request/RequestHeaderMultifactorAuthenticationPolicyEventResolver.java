package org.apereo.cas.web.flow.resolver.impl.mfa.request;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.springframework.web.util.CookieGenerator;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * This is {@link RequestHeaderMultifactorAuthenticationPolicyEventResolver}
 * that attempts to resolve the next event based on the authentication providers of this service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class RequestHeaderMultifactorAuthenticationPolicyEventResolver extends BaseRequestMultifactorAuthenticationPolicyEventResolver {


    private final String mfaRequestHeader;

    public RequestHeaderMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                     final CentralAuthenticationService centralAuthenticationService,
                                                                     final ServicesManager servicesManager,
                                                                     final TicketRegistrySupport ticketRegistrySupport,
                                                                     final CookieGenerator warnCookieGenerator,
                                                                     final AuthenticationServiceSelectionPlan authenticationStrategies,
                                                                     final MultifactorAuthenticationProviderSelector selector,
                                                                     final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator, authenticationStrategies, selector, casProperties);
        mfaRequestHeader = casProperties.getAuthn().getMfa().getRequestHeader();
    }

    @Override
    protected List<String> resolveEventFromHttpRequest(final HttpServletRequest request) {
        final Enumeration<String> values = request.getHeaders(mfaRequestHeader);
        if (values != null) {
            LOGGER.debug("Received request header [{}] as [{}]", mfaRequestHeader, values);
            return Collections.list(values);
        }

        LOGGER.debug("No value could be found for request header [{}]", mfaRequestHeader);
        return null;
    }
}
