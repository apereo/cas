package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.multitenancy.TenantExtractor;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultDelegatedIdentityProviders}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultDelegatedIdentityProviders implements DelegatedIdentityProviders {
    protected final DelegatedIdentityProviderFactory delegatedIdentityProviderFactory;
    protected final TenantExtractor tenantExtractor;

    @Override
    public List<? extends Client> findAllClients(final Service service, final WebContext webContext) {
        val tenant = tenantExtractor.extract(webContext.getRequestURL());
        if (tenant.isPresent()) {
            val definition = tenant.get();
            val providers = definition.bindProperties()
                .map(Unchecked.function(delegatedIdentityProviderFactory::buildFrom))
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
            val policy = definition.getDelegatedAuthenticationPolicy();
            if (policy != null && !policy.getAllowedProviders().isEmpty()) {
                val builtProviders = new ArrayList<>(delegatedIdentityProviderFactory.build());
                builtProviders.removeIf(client -> !policy.getAllowedProviders().contains(client.getName()));
                providers.addAll(builtProviders);
            }
            return providers;
        }

        val providers = delegatedIdentityProviderFactory.build();
        LOGGER.trace("The following clients are built: [{}]", providers);
        return new ArrayList<>(providers);
    }
}
