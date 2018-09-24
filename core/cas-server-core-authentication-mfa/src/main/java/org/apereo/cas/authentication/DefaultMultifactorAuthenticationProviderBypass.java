package org.apereo.cas.authentication;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.RegexUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultMultifactorAuthenticationProviderBypass}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class DefaultMultifactorAuthenticationProviderBypass extends AbstractMultifactorAuthenticationProviderBypass {

    private static final long serialVersionUID = 3720922341350004543L;

    private final Pattern httpRequestRemoteAddressPattern;
    private final Set<Pattern> httpRequestHeaderPatterns;

    public DefaultMultifactorAuthenticationProviderBypass(final MultifactorAuthenticationProviderBypassProperties bypassProperties) {
        super(bypassProperties);

        if (StringUtils.isNotBlank(bypassProperties.getHttpRequestRemoteAddress())) {
            this.httpRequestRemoteAddressPattern = RegexUtils.createPattern(bypassProperties.getHttpRequestRemoteAddress());
        } else {
            this.httpRequestRemoteAddressPattern = RegexUtils.MATCH_NOTHING_PATTERN;
        }

        val values = org.springframework.util.StringUtils.commaDelimitedListToSet(bypassProperties.getHttpRequestHeaders());
        this.httpRequestHeaderPatterns = values.stream().map(RegexUtils::createPattern).collect(Collectors.toSet());
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecute(final Authentication authentication,
                                                                  final RegisteredService registeredService,
                                                                  final MultifactorAuthenticationProvider provider,
                                                                  final HttpServletRequest request) {
        val principal = authentication.getPrincipal();
        LOGGER.debug("Evaluating multifactor authentication bypass properties for principal [{}], service [{}] and provider [{}]",
            principal.getId(), registeredService, provider);
        val bypassByPrincipal = locateMatchingAttributeBasedOnPrincipalAttributes(bypassProperties, principal);
        if (bypassByPrincipal) {
            LOGGER.debug("Bypass rules for principal [{}] indicate the request may be ignored", principal.getId());
            updateAuthenticationToRememberBypass(authentication, provider, principal);
            return false;
        }

        val bypassByAuthn = locateMatchingAttributeBasedOnAuthenticationAttributes(bypassProperties, authentication);
        if (bypassByAuthn) {
            LOGGER.debug("Bypass rules for authentication for principal [{}] indicate the request may be ignored", principal.getId());
            updateAuthenticationToRememberBypass(authentication, provider, principal);
            return false;
        }

        val bypassByAuthnMethod = locateMatchingAttributeValue(
            AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE,
            bypassProperties.getAuthenticationMethodName(),
            authentication.getAttributes(), false
        );
        if (bypassByAuthnMethod) {
            LOGGER.debug("Bypass rules for authentication method [{}] indicate the request may be ignored", bypassProperties.getAuthenticationMethodName());
            updateAuthenticationToRememberBypass(authentication, provider, principal);
            return false;
        }

        val bypassByHandlerName = locateMatchingAttributeValue(
            AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS,
            bypassProperties.getAuthenticationHandlerName(),
            authentication.getAttributes(), false
        );
        if (bypassByHandlerName) {
            LOGGER.debug("Bypass rules for authentication handlers [{}] indicate the request may be ignored", bypassProperties.getAuthenticationHandlerName());
            updateAuthenticationToRememberBypass(authentication, provider, principal);
            return false;
        }

        val bypassByCredType = locateMatchingCredentialType(authentication, bypassProperties.getCredentialClassType());
        if (bypassByCredType) {
            LOGGER.debug("Bypass rules for credential types [{}] indicate the request may be ignored", bypassProperties.getCredentialClassType());
            updateAuthenticationToRememberBypass(authentication, provider, principal);
            return false;
        }

        val bypassByHttpRequest = locateMatchingHttpRequest(authentication, request);
        if (bypassByHttpRequest) {
            LOGGER.debug("Bypass rules for http request indicate the request may be ignored for [{}]", principal.getId());
            updateAuthenticationToRememberBypass(authentication, provider, principal);
            return false;
        }

        val bypassByService = locateMatchingRegisteredServiceForBypass(authentication, registeredService);
        if (bypassByService) {
            updateAuthenticationToRememberBypass(authentication, provider, principal);
            return false;
        }

        updateAuthenticationToForgetBypass(authentication, provider, principal);

        return true;
    }

    /**
     * Locate matching registered service property boolean.
     *
     * @param authentication    the authentication
     * @param registeredService the registered service
     * @return true/false
     */
    protected boolean locateMatchingRegisteredServiceForBypass(final Authentication authentication,
                                                               final RegisteredService registeredService) {
        if (registeredService != null && registeredService.getMultifactorPolicy() != null) {
            return registeredService.getMultifactorPolicy().isBypassEnabled();
        }
        return false;
    }

    /**
     * Locate matching credential type boolean.
     *
     * @param authentication      the authentication
     * @param credentialClassType the credential class type
     * @return the boolean
     */
    protected boolean locateMatchingCredentialType(final Authentication authentication, final String credentialClassType) {
        return StringUtils.isNotBlank(credentialClassType) && authentication.getCredentials()
            .stream()
            .anyMatch(e -> e.getCredentialClass().getName().matches(credentialClassType));
    }

    /**
     * Skip bypass and support event based on authentication attributes.
     *
     * @param bypass the bypass settings for the provider.
     * @param authn  the authn
     * @return the boolean
     */
    protected boolean locateMatchingAttributeBasedOnAuthenticationAttributes(
        final MultifactorAuthenticationProviderBypassProperties bypass, final Authentication authn) {
        return locateMatchingAttributeValue(bypass.getAuthenticationAttributeName(),
            bypass.getAuthenticationAttributeValue(), authn.getAttributes());
    }

    /**
     * Skip bypass and support event based on principal attributes.
     *
     * @param bypass    the bypass properties
     * @param principal the principal
     * @return the boolean
     */
    protected boolean locateMatchingAttributeBasedOnPrincipalAttributes(
        final MultifactorAuthenticationProviderBypassProperties bypass, final Principal principal) {
        return locateMatchingAttributeValue(bypass.getPrincipalAttributeName(), bypass.getPrincipalAttributeValue(), principal.getAttributes());
    }

    /**
     * Locate matching attribute value boolean.
     *
     * @param attrName   the attr name
     * @param attrValue  the attr value
     * @param attributes the attributes
     * @return true/false
     */
    protected boolean locateMatchingAttributeValue(final String attrName, final String attrValue,
                                                   final Map<String, Object> attributes) {
        return locateMatchingAttributeValue(attrName, attrValue, attributes, true);
    }

    /**
     * Evaluate attribute rules for bypass.
     *
     * @param attrName               the attr name
     * @param attrValue              the attr value
     * @param attributes             the attributes
     * @param matchIfNoValueProvided the force match on value
     * @return true a matching attribute name/value is found
     */
    protected boolean locateMatchingAttributeValue(final String attrName, final String attrValue,
                                                   final Map<String, Object> attributes,
                                                   final boolean matchIfNoValueProvided) {
        LOGGER.debug("Locating matching attribute [{}] with value [{}] amongst the attribute collection [{}]", attrName, attrValue, attributes);
        if (StringUtils.isBlank(attrName)) {
            LOGGER.debug("Failed to match since attribute name is undefined");
            return false;
        }

        val names = attributes.entrySet()
            .stream()
            .filter(e -> {
                LOGGER.debug("Attempting to match [{}] against [{}]", attrName, e.getKey());
                return e.getKey().matches(attrName);
            })
            .collect(Collectors.toSet());

        LOGGER.debug("Found [{}] attributes relevant for multifactor authentication bypass", names.size());

        if (names.isEmpty()) {
            return false;
        }

        if (StringUtils.isBlank(attrValue)) {
            LOGGER.debug("No attribute value to match is provided; Match result is set to [{}]", matchIfNoValueProvided);
            return matchIfNoValueProvided;
        }

        val values = names
            .stream()
            .filter(e -> {
                val valuesCol = CollectionUtils.toCollection(e.getValue());
                LOGGER.debug("Matching attribute [{}] with values [{}] against [{}]", e.getKey(), valuesCol, attrValue);
                return valuesCol
                    .stream()
                    .anyMatch(v -> v.toString().matches(attrValue));
            }).collect(Collectors.toSet());

        LOGGER.debug("Matching attribute values remaining are [{}]", values);
        return !values.isEmpty();
    }

    /**
     * Locate matching http request and determine if bypass should be enabled.
     *
     * @param authentication the authentication
     * @param request        the request
     * @return true /false
     */
    protected boolean locateMatchingHttpRequest(final Authentication authentication, final HttpServletRequest request) {
        if (StringUtils.isNotBlank(bypassProperties.getHttpRequestRemoteAddress())) {
            if (httpRequestRemoteAddressPattern.matcher(request.getRemoteAddr()).find()) {
                LOGGER.debug("Http request remote address [{}] matches [{}]", bypassProperties.getHttpRequestRemoteAddress(), request.getRemoteAddr());
                return true;
            }
            if (httpRequestRemoteAddressPattern.matcher(request.getRemoteHost()).find()) {
                LOGGER.debug("Http request remote host [{}] matches [{}]", bypassProperties.getHttpRequestRemoteAddress(), request.getRemoteHost());
                return true;
            }
        }

        if (StringUtils.isNotBlank(bypassProperties.getHttpRequestHeaders())) {
            val headerNames = Collections.list(request.getHeaderNames());
            val matched = this.httpRequestHeaderPatterns.stream()
                .anyMatch(pattern -> headerNames.stream().anyMatch(name -> pattern.matcher(name).matches()));
            if (matched) {
                LOGGER.debug("Http request remote headers [{}] match [{}]", headerNames, bypassProperties.getHttpRequestHeaders());
                return true;
            }
        }

        return false;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
