package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationRequestCustomizer;
import org.apereo.cas.pac4j.client.DelegatedClientIdentityProviderRedirectionStrategy;
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

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    private final Clients clients;

    private final DelegatedAuthenticationAccessStrategyHelper delegatedAuthenticationAccessStrategyHelper;

    private final CasConfigurationProperties casProperties;

    private final List<DelegatedClientAuthenticationRequestCustomizer> delegatedClientAuthenticationRequestCustomizers;

    private final DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy;

    @Override
    public Set<DelegatedClientIdentityProviderConfiguration> produce(final RequestContext context) {
        val currentService = WebUtils.getService(context);
        val service = authenticationRequestServiceSelectionStrategies.resolveService(currentService, WebApplicationService.class);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        val webContext = new JEEContext(request, response);

        LOGGER.debug("Initialized context with request parameters [{}]", webContext.getRequestParameters());
        val allClients = this.clients.findAllClients();
        val providers = new LinkedHashSet<DelegatedClientIdentityProviderConfiguration>(allClients.size());
        allClients
            .stream()
            .filter(client -> client instanceof IndirectClient && isDelegatedClientAuthorizedForService(client, service, request))
            .map(IndirectClient.class::cast)
            .forEach(client -> {
                try {
                    val providerResult = produce(context, client);
                    providerResult.ifPresent(provider -> {
                        providers.add(provider);
                        delegatedClientIdentityProviderRedirectionStrategy.getPrimaryDelegatedAuthenticationProvider(context, service, provider)
                            .ifPresent(p -> WebUtils.putDelegatedAuthenticationProviderPrimary(context, p));
                    });
                } catch (final Exception e) {
                    LOGGER.error("Cannot process client [{}]", client);
                    LoggingUtils.error(LOGGER, e);
                }
            });

        if (!providers.isEmpty()) {
            val selectionType = casProperties.getAuthn().getPac4j().getCore().getDiscoverySelection().getSelectionType();
            switch (selectionType) {
                case DYNAMIC:
                    WebUtils.putDelegatedAuthenticationProviderConfigurations(context, new HashSet<>());
                    WebUtils.putDelegatedAuthenticationDynamicProviderSelection(context, Boolean.TRUE);
                    break;
                case MENU:
                default:
                    WebUtils.putDelegatedAuthenticationProviderConfigurations(context, providers);
                    WebUtils.putDelegatedAuthenticationDynamicProviderSelection(context, Boolean.FALSE);
                    break;
            }
            
        } else if (response.getStatus() != HttpStatus.UNAUTHORIZED.value()) {
            LOGGER.warn("No delegated authentication providers could be determined based on the provided configuration. "
                + "Either no clients are configured, or the current access strategy rules prohibit CAS from using authentication providers");
        }
        return providers;
    }

    @Override
    public Optional<DelegatedClientIdentityProviderConfiguration> produce(final RequestContext requestContext,
                                                                          final IndirectClient client) {
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
        val webContext = new JEEContext(request, response);

        val currentService = WebUtils.getService(requestContext);
        LOGGER.debug("Initializing client [{}] with request parameters [{}] and service [{}]",
            client, requestContext.getRequestParameters(), currentService);
        client.init();

        if (delegatedClientAuthenticationRequestCustomizers.isEmpty()
            || delegatedClientAuthenticationRequestCustomizers.stream().anyMatch(c -> c.isAuthorized(webContext, client, currentService))) {
            return DelegatedClientIdentityProviderConfigurationFactory.builder()
                .client(client)
                .webContext(webContext)
                .service(currentService)
                .casProperties(casProperties)
                .build()
                .resolve();
        }
        return Optional.empty();
    }

    private boolean isDelegatedClientAuthorizedForService(final Client client, final Service service,
                                                          final HttpServletRequest request) {
        return delegatedAuthenticationAccessStrategyHelper.isDelegatedClientAuthorizedForService(client, service, request);
    }
}
