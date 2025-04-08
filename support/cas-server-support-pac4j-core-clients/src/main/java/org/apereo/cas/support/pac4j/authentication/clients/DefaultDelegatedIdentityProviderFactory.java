package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.github.benmanes.caffeine.cache.Cache;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.BaseClient;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.Closeable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * This is {@link DefaultDelegatedIdentityProviderFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultDelegatedIdentityProviderFactory extends BaseDelegatedIdentityProviderFactory implements DisposableBean {

    public DefaultDelegatedIdentityProviderFactory(
        final CasConfigurationProperties casProperties,
        final Collection<DelegatedClientFactoryCustomizer> customizers,
        final CasSSLContext casSSLContext,
        final Cache<String, List<BaseClient>> clientsCache,
        final ConfigurableApplicationContext applicationContext) {
        super(casProperties, customizers, casSSLContext, clientsCache, applicationContext);
    }

    @Override
    protected List<BaseClient> loadIdentityProviders() throws Exception {
        return buildFrom(casProperties);
    }

    @Override
    public void destroy() {
        Optional.ofNullable(getCachedClients())
            .stream()
            .filter(Closeable.class::isInstance)
            .map(Closeable.class::cast)
            .forEach(Unchecked.consumer(Closeable::close));
    }
}
