package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.authentication.principal.provision.DelegatedAuthenticationFailureException;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;
import org.apereo.cas.web.DelegatedClientIdentityProviderConfigurationFactory;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.jee.context.JEEContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.RequestContext;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultDelegatedClientIdentityProviderConfigurationProducer}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultDelegatedClientIdentityProviderConfigurationProducer implements DelegatedClientIdentityProviderConfigurationProducer {
    private final ObjectProvider<DelegatedClientAuthenticationConfigurationContext> configurationContext;

    @Override
    public Set<DelegatedClientIdentityProviderConfiguration> produce(final RequestContext context) throws Throwable {
        val currentService = WebUtils.getService(context);

        val selectionStrategies = configurationContext.getObject().getAuthenticationRequestServiceSelectionStrategies();
        val service = selectionStrategies.resolveService(currentService, WebApplicationService.class);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(context);
        val webContext = new JEEContext(request, response);

        LOGGER.debug("Initialized context with request parameters [{}]", webContext.getRequestParameters());

        val allClients = findAllClients(service, webContext);
        val providers = allClients
            .stream()
            .filter(client -> client instanceof IndirectClient
                && isDelegatedClientAuthorizedForService(client, service, context))
            .map(IndirectClient.class::cast)
            .map(Unchecked.function(client -> produce(context, client)))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .sorted(Comparator.comparing(DelegatedClientIdentityProviderConfiguration::getName))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        val delegatedClientIdentityProviderRedirectionStrategy = configurationContext.getObject().getDelegatedClientIdentityProviderRedirectionStrategy();
        delegatedClientIdentityProviderRedirectionStrategy.select(context, service, providers)
            .ifPresent(p -> DelegationWebflowUtils.putDelegatedAuthenticationProviderPrimary(context, p));

        if (!providers.isEmpty()) {
            val casProperties = configurationContext.getObject().getCasProperties();
            val selectionType = casProperties.getAuthn().getPac4j().getCore().getDiscoverySelection().getSelectionType();
            switch (selectionType) {
                case DYNAMIC -> {
                    DelegationWebflowUtils.putDelegatedAuthenticationProviderConfigurations(context, new HashSet<>());
                    DelegationWebflowUtils.putDelegatedAuthenticationDynamicProviderSelection(context, Boolean.TRUE);
                }
                case MENU -> {
                    DelegationWebflowUtils.putDelegatedAuthenticationProviderConfigurations(context, providers);
                    DelegationWebflowUtils.putDelegatedAuthenticationDynamicProviderSelection(context, Boolean.FALSE);
                }
            }

        } else if (response.getStatus() != HttpStatus.UNAUTHORIZED.value()) {
            LOGGER.info("No delegated authentication providers could be determined based on the provided configuration. "
                + "Either no identity providers are configured, or the current access strategy rules prohibit CAS from using authentication providers");
        }
        return providers;
    }

    @Override
    public Optional<DelegatedClientIdentityProviderConfiguration> produce(final RequestContext requestContext,
                                                                          final IndirectClient client) {
        return FunctionUtils.doAndHandle(() -> {
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val webContext = new JEEContext(request, response);

            val currentService = WebUtils.getService(requestContext);
            initializeClientIdentityProvider(client, requestContext);

            val customizers = configurationContext.getObject().getDelegatedClientAuthenticationRequestCustomizers();
            if (customizers.isEmpty() || customizers.stream()
                .filter(BeanSupplier::isNotProxy)
                .anyMatch(Unchecked.predicate(clientConfig -> clientConfig.isAuthorized(webContext, client, currentService)))) {
                return DelegatedClientIdentityProviderConfigurationFactory.builder()
                    .client(client)
                    .webContext(webContext)
                    .service(currentService)
                    .casProperties(configurationContext.getObject().getCasProperties())
                    .build()
                    .resolve();
            }
            return Optional.<DelegatedClientIdentityProviderConfiguration>empty();
        }, throwable -> Optional.<DelegatedClientIdentityProviderConfiguration>empty()).get();
    }

    protected void initializeClientIdentityProvider(final IndirectClient client, final RequestContext context) throws Throwable {
        if (!client.isInitialized()) {
            val currentService = WebUtils.getService(context);
            LOGGER.trace("Initializing client [{}] with request parameters [{}] and service [{}]",
                client, context.getRequestParameters(), currentService);
            client.init(true);
        }
        FunctionUtils.throwIf(!client.isInitialized(), DelegatedAuthenticationFailureException::new);
    }

    protected boolean isDelegatedClientAuthorizedForService(final Client client,
                                                            final Service service,
                                                            final RequestContext context) {
        return configurationContext.getObject().getDelegatedClientIdentityProviderAuthorizers()
            .stream()
            .allMatch(Unchecked.predicate(authz -> authz.isDelegatedClientAuthorizedForService(client, service, context)));
    }

    protected List<? extends Client> findAllClients(final WebApplicationService service, final WebContext webContext) {
        val clients = configurationContext.getObject().getIdentityProviders();
        return clients.findAllClients(service, webContext);
    }
}
