package org.apereo.cas.authentication.bypass;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.MultifactorAuthenticationProviderBypassProperties;
import org.apereo.cas.services.RegisteredService;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serial;

/**
 * Multifactor Bypass based on Principal Attributes.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@Slf4j
@Getter
public class PrincipalMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    @Serial
    private static final long serialVersionUID = -7553435418344342672L;

    private final String attributeName;

    private final String attributeValue;

    public PrincipalMultifactorAuthenticationProviderBypassEvaluator(final MultifactorAuthenticationProviderBypassProperties bypassProperties,
                                                                     final String providerId, final ApplicationContext applicationContext) {
        this(bypassProperties.getPrincipalAttributeName(), bypassProperties.getPrincipalAttributeValue(), providerId, applicationContext);
    }

    public PrincipalMultifactorAuthenticationProviderBypassEvaluator(final String attributeName, final String attributeValue,
                                                                     final String providerId, final ApplicationContext applicationContext) {
        super(providerId, applicationContext);
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                          final RegisteredService registeredService,
                                                                          final MultifactorAuthenticationProvider provider,
                                                                          final HttpServletRequest request) {
        val principal = resolvePrincipal(authentication.getPrincipal());
        LOGGER.debug("Evaluating multifactor authentication bypass properties for principal [{}], service [{}] and provider [{}]",
            principal.getId(), registeredService, provider);
        val bypass = locateMatchingAttributeValue(this.attributeName, StringUtils.commaDelimitedListToSet(attributeValue),
            principal.getAttributes(), true);
        if (bypass) {
            LOGGER.debug("Bypass rules for principal [{}] indicate the request may be ignored", principal.getId());
            return false;
        }
        return true;
    }
}
