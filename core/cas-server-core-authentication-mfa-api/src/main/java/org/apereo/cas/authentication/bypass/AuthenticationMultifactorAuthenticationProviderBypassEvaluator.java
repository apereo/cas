package org.apereo.cas.authentication.bypass;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Multifactor Bypass Provider based on Authentication.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@Slf4j
public class AuthenticationMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    @Serial
    private static final long serialVersionUID = 5582655921143779773L;

    private final MultifactorAuthenticationProviderBypassProperties bypassProperties;

    public AuthenticationMultifactorAuthenticationProviderBypassEvaluator(
        final MultifactorAuthenticationProviderBypassProperties bypassProperties,
        final String providerId,
        final ConfigurableApplicationContext applicationContext) {
        super(providerId, applicationContext);
        this.bypassProperties = bypassProperties;
    }

    protected static boolean locateMatchingAttributeBasedOnAuthenticationAttributes(
        final MultifactorAuthenticationProviderBypassProperties bypass, final Authentication authn) {
        return locateMatchingAttributeValue(bypass.getAuthenticationAttributeName(),
            StringUtils.commaDelimitedListToSet(bypass.getAuthenticationAttributeValue()),
            authn.getAttributes(), false);
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(
        final Authentication authentication,
        @Nullable final RegisteredService registeredService,
        final MultifactorAuthenticationProvider provider,
        @Nullable final HttpServletRequest request) {
        val principal = resolvePrincipal(authentication.getPrincipal());
        val bypassByAuthn = locateMatchingAttributeBasedOnAuthenticationAttributes(bypassProperties, authentication);
        if (bypassByAuthn) {
            LOGGER.debug("Bypass rules for authentication for principal [{}] indicate the request may be ignored", principal.getId());
            return false;
        }
        val bypassByAuthnMethod = locateMatchingAttributeValue(
            AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE,
            StringUtils.commaDelimitedListToSet(bypassProperties.getAuthenticationMethodName()),
            authentication.getAttributes(), false
        );
        if (bypassByAuthnMethod) {
            LOGGER.debug("Bypass rules for authentication method [{}] indicate the request may be ignored", bypassProperties.getAuthenticationMethodName());
            return false;
        }

        val bypassByHandlerName = locateMatchingAttributeValue(
            AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS,
            StringUtils.commaDelimitedListToSet(bypassProperties.getAuthenticationHandlerName()),
            authentication.getAttributes(), false
        );
        if (bypassByHandlerName) {
            LOGGER.debug("Bypass rules for authentication handlers [{}] indicate the request may be ignored", bypassProperties.getAuthenticationHandlerName());
            return false;
        }

        return true;
    }
}
