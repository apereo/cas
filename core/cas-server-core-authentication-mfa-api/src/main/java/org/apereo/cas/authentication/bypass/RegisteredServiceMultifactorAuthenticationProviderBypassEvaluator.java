package org.apereo.cas.authentication.bypass;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ConfigurableApplicationContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Multifactor Bypass Provider based on Service Multifactor Policy.
 *
 * @author Travis Schmidt
 * @since 6.0
 */
@Slf4j
public class RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator extends BaseMultifactorAuthenticationProviderBypassEvaluator {
    @Serial
    private static final long serialVersionUID = -3553888418344342672L;

    public RegisteredServiceMultifactorAuthenticationProviderBypassEvaluator(final String providerId, final ConfigurableApplicationContext applicationContext) {
        super(providerId, applicationContext);
    }

    @Override
    public boolean shouldMultifactorAuthenticationProviderExecuteInternal(final Authentication authentication,
                                                                          @Nullable final RegisteredService registeredService,
                                                                          final MultifactorAuthenticationProvider provider,
                                                                          @Nullable final HttpServletRequest request) {
        var shouldExecute = registeredService == null
            || registeredService.getMultifactorAuthenticationPolicy() == null
            || !registeredService.getMultifactorAuthenticationPolicy().isBypassEnabled();
        if (shouldExecute) {
            shouldExecute = shouldMultifactorAuthenticationProviderExecuteForPrincipal(authentication, registeredService, provider, request);
        }
        if (shouldExecute) {
            shouldExecute = shouldMultifactorAuthenticationProviderExecuteForRequest(authentication, registeredService, provider, request);
        }
        return shouldExecute;
    }

    protected boolean shouldMultifactorAuthenticationProviderExecuteForRequest(
        final Authentication authentication, final @Nullable RegisteredService registeredService,
        final MultifactorAuthenticationProvider provider, final @Nullable HttpServletRequest request) {
        if (registeredService != null) {
            val mfaPolicy = registeredService.getMultifactorAuthenticationPolicy();
            val bypassEnabled = mfaPolicy != null && StringUtils.isNotBlank(mfaPolicy.getBypassForRequestIpAddress());

            if (bypassEnabled) {
                val remoteAddr = Objects.requireNonNull(request).getRemoteAddr();
                val bypass = RegexUtils.find(mfaPolicy.getBypassForRequestIpAddress(), remoteAddr);
                if (bypass) {
                    LOGGER.debug("Bypass rules for IP [{}] indicate the request may be ignored", remoteAddr);
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean shouldMultifactorAuthenticationProviderExecuteForPrincipal(
        final Authentication authentication,
        @Nullable final RegisteredService registeredService,
        final MultifactorAuthenticationProvider provider,
        @Nullable final HttpServletRequest request) {
        if (registeredService != null) {
            val mfaPolicy = registeredService.getMultifactorAuthenticationPolicy();
            val bypassEnabled = mfaPolicy != null
                && StringUtils.isNotBlank(mfaPolicy.getBypassPrincipalAttributeName())
                && StringUtils.isNotBlank(mfaPolicy.getBypassPrincipalAttributeValue());

            if (bypassEnabled) {
                val principal = resolvePrincipal(authentication.getPrincipal());

                val matchingAttributes = locateMatchingAttributeName(principal.getAttributes(), mfaPolicy.getBypassPrincipalAttributeName());
                if (matchingAttributes.isEmpty()) {
                    LOGGER.debug("No matching principal attribute name from [{}] can be found for [{}]",
                        principal.getAttributes().keySet(), mfaPolicy.getBypassPrincipalAttributeName());
                    return !mfaPolicy.isBypassIfMissingPrincipalAttribute();
                }

                val bypass = locateMatchingAttributeValue(mfaPolicy.getBypassPrincipalAttributeName(),
                    org.springframework.util.StringUtils.commaDelimitedListToSet(mfaPolicy.getBypassPrincipalAttributeValue()),
                    principal.getAttributes(), true);
                if (bypass) {
                    LOGGER.debug("Bypass rules for principal [{}] indicate the request may be ignored", principal.getId());
                    return false;
                }
            }
        }
        return true;
    }
}
