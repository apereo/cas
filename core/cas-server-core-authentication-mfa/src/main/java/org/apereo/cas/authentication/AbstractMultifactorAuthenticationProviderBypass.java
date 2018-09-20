package org.apereo.cas.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;

/**
 * This is base for implementations of MultifactorAuthenticationProviderBypass.
 *
 * @author Travis Schmidt
 * @since 5.3.4
 */
@Slf4j
public abstract class AbstractMultifactorAuthenticationProviderBypass implements MultifactorAuthenticationProviderBypass {

    /**
     * Bypass settings used for configuration.
     */
    protected final MultifactorAuthenticationProviderBypassProperties bypassProperties;

    public AbstractMultifactorAuthenticationProviderBypass(final MultifactorAuthenticationProviderBypassProperties bypass) {
        this.bypassProperties = bypass;
    }

    /**
     * Method will remove any previous bypass set in the authentication.
     *
     * @param authentication - the authentication
     * @param provider - the provider
     * @param principal - the principal
     */
    protected void updateAuthenticationToForgetBypass(final Authentication authentication,
                                                             final MultifactorAuthenticationProvider provider,
                                                             final Principal principal) {
        LOGGER.debug("Bypass rules for service [{}] indicate the request may be ignored", principal.getId());
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, Boolean.FALSE);
        LOGGER.debug("Updated authentication session to remember bypass for [{}] via [{}]", provider.getId(), AUTHENTICATION_ATTRIBUTE_BYPASS_MFA);
    }

    /**
     * Method will set the bypass into the authentication.
     *
     * @param authentication - the authentication
     * @param provider - the provider
     * @param principal - the principal
     */
    protected void updateAuthenticationToRememberBypass(final Authentication authentication, final MultifactorAuthenticationProvider provider,
                                                               final Principal principal) {
        LOGGER.debug("Bypass rules for service [{}] indicate the request may NOT be ignored", principal.getId());
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA, Boolean.TRUE);
        authentication.addAttribute(AUTHENTICATION_ATTRIBUTE_BYPASS_MFA_PROVIDER, provider.getId());
        LOGGER.debug("Updated authentication session to NOT remember bypass for [{}] via [{}]", provider.getId(), AUTHENTICATION_ATTRIBUTE_BYPASS_MFA);
    }
}
