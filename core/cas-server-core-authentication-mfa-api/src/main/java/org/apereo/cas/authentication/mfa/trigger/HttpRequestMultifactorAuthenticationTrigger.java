package org.apereo.cas.authentication.mfa.trigger;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderAbsentException;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link HttpRequestMultifactorAuthenticationTrigger}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Slf4j
@RequiredArgsConstructor
public class HttpRequestMultifactorAuthenticationTrigger implements MultifactorAuthenticationTrigger {
    private final CasConfigurationProperties casProperties;
    private final ApplicationContext applicationContext;

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest, final Service service) {
        if (authentication == null) {
            LOGGER.debug("No authentication is available to determine event for principal");
            return Optional.empty();
        }

        val values = resolveEventFromHttpRequest(httpServletRequest);
        if (values != null && !values.isEmpty()) {
            LOGGER.debug("Received request as [{}]", values);

            val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
            if (providerMap.isEmpty()) {
                LOGGER.error("No multifactor authentication providers are available in the application context to satisfy [{}]", values);
                throw new AuthenticationException(new MultifactorAuthenticationProviderAbsentException());
            }

            val providerFound = MultifactorAuthenticationUtils.resolveProvider(providerMap, values.get(0));
            if (providerFound.isPresent()) {
                return providerFound;
            }

            LOGGER.warn("No multifactor provider could be found for request parameter [{}]", values);
            throw new AuthenticationException();
        }
        return Optional.empty();
    }

    /**
     * Resolve event from http request.
     *
     * @param request the request
     * @return the list
     */
    protected List<String> resolveEventFromHttpRequest(final HttpServletRequest request) {
        val mfaRequestHeader = casProperties.getAuthn().getMfa().getRequestHeader();
        val headers = request.getHeaders(mfaRequestHeader);
        if (headers != null && headers.hasMoreElements()) {
            LOGGER.debug("Received request header [{}] as [{}]", mfaRequestHeader, headers);
            return Collections.list(headers);
        }

        val mfaRequestParameter = casProperties.getAuthn().getMfa().getRequestParameter();
        val params = request.getParameterValues(mfaRequestParameter);
        if (params != null && params.length > 0) {
            LOGGER.debug("Received request parameter [{}] as [{}]", mfaRequestParameter, params);
            return Arrays.stream(params).collect(Collectors.toList());
        }

        val attributeName = casProperties.getAuthn().getMfa().getSessionAttribute();
        val session = request.getSession(false);
        var attributeValue = Optional.ofNullable(session).map(httpSession -> httpSession.getAttribute(attributeName)).orElse(null);
        if (attributeValue == null) {
            LOGGER.trace("No value could be found for session attribute [{}]. Checking request attributes...", attributeName);
            attributeValue = request.getAttribute(attributeName);
        }

        if (attributeValue == null) {
            LOGGER.trace("No value could be found for [{}]", attributeName);
            return null;
        }

        val values = CollectionUtils.toCollection(attributeValue);
        LOGGER.debug("Found values [{}] mapped to attribute name [{}]", values, attributeName);
        return values.stream().map(Object::toString).collect(Collectors.toList());
    }
}
