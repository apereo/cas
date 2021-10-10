package org.apereo.cas.web.flow;

import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public interface DelegatedClientIdentityProviderConfigurationPostProcessor {

    /**
     * No op.
     *
     * @return the delegated client identity provider configuration post processor
     */
    static DelegatedClientIdentityProviderConfigurationPostProcessor noOp() {
        return (context, providers) -> {
        };
    }

    /**
     * Process.
     *
     * @param context   the context
     * @param providers the providers
     */
    void process(RequestContext context, Set<DelegatedClientIdentityProviderConfiguration> providers);
}
