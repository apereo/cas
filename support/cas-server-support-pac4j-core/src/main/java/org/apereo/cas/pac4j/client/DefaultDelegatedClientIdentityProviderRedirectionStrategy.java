package org.apereo.cas.pac4j.client;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.delegation.DelegationAutoRedirectTypes;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.DelegationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.RequestContext;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link DefaultDelegatedClientIdentityProviderRedirectionStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultDelegatedClientIdentityProviderRedirectionStrategy implements DelegatedClientIdentityProviderRedirectionStrategy {
    protected final ServicesManager servicesManager;

    protected final CasCookieBuilder delegatedAuthenticationCookieBuilder;

    protected final CasConfigurationProperties casProperties;

    protected final ConfigurableApplicationContext applicationContext;

    @Override
    public Optional<DelegatedClientIdentityProviderConfiguration> select(
        final RequestContext context,
        final WebApplicationService service,
        final Set<DelegatedClientIdentityProviderConfiguration> providers) {

        for (val provider : providers) {
            if (service != null) {
                val registeredService = servicesManager.findServiceBy(service);
                val delegatedPolicy = registeredService.getAccessStrategy().getDelegatedAuthenticationPolicy();
                if (delegatedPolicy != null) {
                    if (delegatedPolicy.isExclusiveToProvider(provider.getName())) {
                        LOGGER.trace("Registered service [{}] is exclusively allowed to use provider [{}]", registeredService, provider);
                        provider.setAutoRedirectType(DelegationAutoRedirectTypes.SERVER);
                        return Optional.of(provider);
                    }

                    if (StringUtils.isNotBlank(delegatedPolicy.getSelectionStrategy())) {
                        return ApplicationContextProvider.getScriptResourceCacheManager()
                            .map(Unchecked.function(cacheMgr -> {
                                val strategy = SpringExpressionLanguageValueResolver.getInstance()
                                    .resolve(delegatedPolicy.getSelectionStrategy());
                                val script = cacheMgr.resolveScriptableResource(strategy,
                                    String.valueOf(registeredService.getId()), registeredService.getName());
                                val args = CollectionUtils.<String, Object>wrap("requestContext", context,
                                    "service", service, "registeredService", registeredService,
                                    "providers", providers, "applicationContext", applicationContext,
                                    "logger", LOGGER);
                                script.setBinding(args);
                                val result = script.execute(args.values().toArray(), DelegatedClientIdentityProviderConfiguration.class, false);
                                return Optional.ofNullable(result);
                            }))
                            .orElseThrow(() -> new RuntimeException("No groovy script cache manager"));
                    }
                }
            }

            if (DelegationWebflowUtils.getDelegatedAuthenticationProviderPrimary(context) == null
                && provider.getAutoRedirectType() != DelegationAutoRedirectTypes.NONE) {
                LOGGER.trace("Provider [{}] is configured to auto-redirect", provider);
                return Optional.of(provider);
            }

            val cookieProps = casProperties.getAuthn().getPac4j().getCookie();
            if (cookieProps.isEnabled()) {
                val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
                val cookieValue = delegatedAuthenticationCookieBuilder.retrieveCookieValue(request);
                if (Strings.CI.equals(cookieValue, provider.getName())) {
                    LOGGER.trace("Provider [{}] is chosen via cookie value preference as primary", provider);
                    provider.setAutoRedirectType(DelegationAutoRedirectTypes.SERVER);
                    return Optional.of(provider);
                }
            }
        }
        return Optional.empty();
    }
}
