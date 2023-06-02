package org.apereo.cas.web.flow;

import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

import org.pac4j.core.client.IndirectClient;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;
import java.util.Set;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationProducer}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface DelegatedClientIdentityProviderConfigurationProducer {
    /**
     * Default bean name.
     */
    String BEAN_NAME = "delegatedClientIdentityProviderConfigurationProducer";

    /**
     * Produce.
     *
     * @param context the context
     * @return the set
     */
    Set<DelegatedClientIdentityProviderConfiguration> produce(RequestContext context);

    /**
     * Produce.
     *
     * @param requestContext the request context
     * @param client         the client
     * @return the optional
     */
    Optional<DelegatedClientIdentityProviderConfiguration> produce(RequestContext requestContext, IndirectClient client);
}
