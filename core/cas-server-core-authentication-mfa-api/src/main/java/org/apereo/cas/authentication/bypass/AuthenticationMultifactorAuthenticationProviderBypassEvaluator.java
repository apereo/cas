package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.servlet.http.HttpServletRequest;

/**
 * Multifactor Bypass Provider based on Authentication.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@Slf4j
public class AuthenticationMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    private static final long serialVersionUID = 5582655921143779773L;
    private final MultifactorAuthenticationProviderBypassProperties bypassProperties;

    public AuthenticationMultifactorAuthenticationProviderBypassEvaluator(final MultifactorAuthenticationProviderBypassProperties bypassProperties,
                                                                          final String providerId) {
        super(providerId);
        this.bypassProperties = bypassProperties;
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                          final RegisteredService registeredService,
                                                                          final MultifactorAuthenticationProvider provider,
                                                                          final HttpServletRequest request) {
        val principal = authentication.getPrincipal();
        val bypassByAuthn = locateMatchingAttributeBasedOnAuthenticationAttributes(bypassProperties, authentication);
        if (bypassByAuthn) {
            LOGGER.debug("Bypass rules for authentication for principal [{}] indicate the request may be ignored", principal.getId());
            return false;
        }

        val bypassByAuthnMethod = locateMatchingAttributeValue(
            AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE,
            bypassProperties.getAuthenticationMethodName(),
            authentication.getAttributes(), false
        );
        if (bypassByAuthnMethod) {
            LOGGER.debug("Bypass rules for authentication method [{}] indicate the request may be ignored", bypassProperties.getAuthenticationMethodName());
            return false;
        }

        val bypassByHandlerName = locateMatchingAttributeValue(
            AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS,
            bypassProperties.getAuthenticationHandlerName(),
            authentication.getAttributes(), false
        );
        if (bypassByHandlerName) {
            LOGGER.debug("Bypass rules for authentication handlers [{}] indicate the request may be ignored", bypassProperties.getAuthenticationHandlerName());
            return false;
        }

        return true;
    }

    /**
     * Skip bypass and support event based on authentication attributes.
     *
     * @param bypass the bypass settings for the provider.
     * @param authn  the authn
     * @return true/false
     */
    protected static boolean locateMatchingAttributeBasedOnAuthenticationAttributes(
        final MultifactorAuthenticationProviderBypassProperties bypass, final Authentication authn) {
        return locateMatchingAttributeValue(bypass.getAuthenticationAttributeName(),
            bypass.getAuthenticationAttributeValue(), authn.getAttributes(), false);
    }
}
