package org.apereo.cas.pac4j.client;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

/**
 * This is {@link DefaultDelegatedClientIdentityProviderRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultDelegatedClientIdentityProviderRedirectionStrategy implements DelegatedClientIdentityProviderRedirectionStrategy {
    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;

    /**
     * Delegated authentication cookie builder.
     */
    protected final CasCookieBuilder delegatedAuthenticationCookieBuilder;

    /**
     * CAS properties.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public Optional<DelegatedClientIdentityProviderConfiguration> getPrimaryDelegatedAuthenticationProvider(final RequestContext context,
                                                                                                            final WebApplicationService service,
                                                                                                            final DelegatedClientIdentityProviderConfiguration provider) {
        if (service != null) {
            val registeredService = servicesManager.findServiceBy(service);
            val delegatedPolicy = registeredService.getAccessStrategy().getDelegatedAuthenticationPolicy();
            if (delegatedPolicy.isExclusive() && delegatedPolicy.getAllowedProviders().size() == 1
                && provider.getName().equalsIgnoreCase(delegatedPolicy.getAllowedProviders().iterator().next())) {
                LOGGER.trace("Registered service [{}] is exclusively allowed to use provider [{}]", registeredService, provider);
                provider.setAutoRedirect(true);
                return Optional.of(provider);
            }
        }

        if (WebUtils.getDelegatedAuthenticationProviderPrimary(context) == null && provider.isAutoRedirect()) {
            LOGGER.trace("Provider [{}] is configured to auto-redirect", provider);
            return Optional.of(provider);
        }

        val cookieProps = casProperties.getAuthn().getPac4j().getCookie();
        if (cookieProps.isEnabled()) {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
            val cookieValue = delegatedAuthenticationCookieBuilder.retrieveCookieValue(request);
            if (StringUtils.equalsIgnoreCase(cookieValue, provider.getName())) {
                LOGGER.trace("Provider [{}] is chosen via cookie value preference as primary", provider);
                provider.setAutoRedirect(true);
                return Optional.of(provider);
            }
        }
        return Optional.empty();
    }
}
