package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.validation.DelegatedAuthenticationAccessStrategyHelper;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfigurationFactory;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.JEEContext;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link DefaultDelegatedClientIdentityProviderConfigurationProducer}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultDelegatedClientIdentityProviderConfigurationProducer implements DelegatedClientIdentityProviderConfigurationProducer {
    /**
     * The Services manager.
     */
    private final ServicesManager servicesManager;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    private final Clients clients;

    private final DelegatedAuthenticationAccessStrategyHelper delegatedAuthenticationAccessStrategyHelper;

    private final CasConfigurationProperties casProperties;

    /**
     * Determine auto redirect policy for provider.
     *
     * @param context  the context
     * @param service  the service
     * @param provider the provider
     */
    protected void determineAutoRedirectPolicyForProvider(final RequestContext context,
                                                          final WebApplicationService service,
                                                          final DelegatedClientIdentityProviderConfiguration provider) {
        if (service != null) {
            val registeredService = servicesManager.findServiceBy(service);
            val delegatedPolicy = registeredService.getAccessStrategy().getDelegatedAuthenticationPolicy();
            if (delegatedPolicy.isExclusive() && delegatedPolicy.getAllowedProviders().size() == 1
                && provider.getName().equalsIgnoreCase(delegatedPolicy.getAllowedProviders().iterator().next())) {
                LOGGER.trace("Registered service [{}] is exclusively allowed to use provider [{}]", registeredService, provider);
                provider.setAutoRedirect(true);
                WebUtils.putDelegatedAuthenticationProviderPrimary(context, provider);
            }
        }

        if (WebUtils.getDelegatedAuthenticationProviderPrimary(context) == null && provider.isAutoRedirect()) {
            LOGGER.trace("Provider [{}] is configured to auto-redirect", provider);
            WebUtils.putDelegatedAuthenticationProviderPrimary(context, provider);
        }
    }

    private boolean isDelegatedClientAuthorizedForService(final Client client,
                                                          final Service service) {
        return delegatedAuthenticationAccessStrategyHelper.isDelegatedClientAuthorizedForService(client, service);
    }

    @Override
    public Set<DelegatedClientIdentityProviderConfiguration> produce(final RequestContext context) {
        val currentService = WebUtils.getService(context);
        val service = authenticationRequestServiceSelectionStrategies.resolveService(currentService, WebApplicationService.class);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        val webContext = new JEEContext(request, response);

        LOGGER.debug("Initialized context with request parameters [{}]", webContext.getRequestParameters());
        val allClients = this.clients.findAllClients();
        val urls = new LinkedHashSet<DelegatedClientIdentityProviderConfiguration>(allClients.size());
        allClients
            .stream()
            .filter(client -> client instanceof IndirectClient && isDelegatedClientAuthorizedForService(client, service))
            .map(IndirectClient.class::cast)
            .forEach(client -> {
                try {
                    val provider = produce(context, client);
                    provider.ifPresent(p -> {
                        urls.add(p);
                        determineAutoRedirectPolicyForProvider(context, service, p);
                    });
                } catch (final Exception e) {
                    LOGGER.error("Cannot process client [{}]", client);
                    LoggingUtils.error(LOGGER, e);
                }
            });

        if (!urls.isEmpty()) {
            WebUtils.putDelegatedAuthenticationProviderConfigurations(context, urls);
        } else if (response.getStatus() != HttpStatus.UNAUTHORIZED.value()) {
            LOGGER.warn("No delegated authentication providers could be determined based on the provided configuration. "
                + "Either no clients are configured, or the current access strategy rules prohibit CAS from using authentication providers");
        }
        return urls;
    }

    @Override
    public Optional<DelegatedClientIdentityProviderConfiguration> produce(final RequestContext requestContext,
                                                                          final IndirectClient client) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);

        val currentService = WebUtils.getService(requestContext);
        val service = authenticationRequestServiceSelectionStrategies.resolveService(currentService, WebApplicationService.class);
        LOGGER.debug("Initializing client [{}] with request parameters [{}]", client, requestContext.getRequestParameters());
        client.init();
        return DelegatedClientIdentityProviderConfigurationFactory.builder()
            .client(client)
            .webContext(webContext)
            .service(service)
            .casProperties(casProperties)
            .build()
            .resolve();
    }
}
