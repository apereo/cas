package org.apereo.cas.web.flow;

import org.apereo.cas.web.DelegatedClientIdentityProviderConfiguration;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.webflow.execution.RequestContext;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;

/**
 * This is {@link DelegatedClientIdentityProviderConfigurationPostProcessor}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@FunctionalInterface
public interface DelegatedClientIdentityProviderConfigurationPostProcessor extends Closeable, DisposableBean {

    /**
     * No op.
     *
     * @return the delegated client identity provider configuration post processor
     */
    static DelegatedClientIdentityProviderConfigurationPostProcessor noOp() {
        return (context, providers) -> {
        };
    }

    @Override
    default void close() throws IOException {
    }

    @Override
    default void destroy() throws Exception {
        close();
    }

    /**
     * Process.
     *
     * @param context   the context
     * @param providers the providers
     */
    void process(RequestContext context, Set<DelegatedClientIdentityProviderConfiguration> providers);
}
