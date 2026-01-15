package org.apereo.cas.support.pac4j.authentication.clients;

import module java.base;
import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import com.github.benmanes.caffeine.cache.Cache;
import org.jspecify.annotations.NonNull;
import org.pac4j.core.client.BaseClient;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link DefaultDelegatedIdentityProviderFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class DefaultDelegatedIdentityProviderFactory extends BaseDelegatedIdentityProviderFactory {

    public DefaultDelegatedIdentityProviderFactory(
        final CasConfigurationProperties casProperties,
        final Collection<DelegatedClientFactoryCustomizer> customizers,
        final CasSSLContext casSSLContext,
        final Cache<@NonNull String, List<BaseClient>> clientsCache,
        final ConfigurableApplicationContext applicationContext) {
        super(casProperties, customizers, casSSLContext, clientsCache, applicationContext);
    }

    @Override
    protected List<BaseClient> load() throws Exception {
        return buildFrom(casProperties);
    }
}
