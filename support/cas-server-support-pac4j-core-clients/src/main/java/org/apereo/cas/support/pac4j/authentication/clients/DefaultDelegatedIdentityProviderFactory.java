package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;

import com.github.benmanes.caffeine.cache.Cache;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Collection;
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
        final ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory,
        final Cache<String, Collection<IndirectClient>> clientsCache) {
        super(casProperties, customizers, casSSLContext, samlMessageStoreFactory, clientsCache);
    }

    @Override
    protected Collection<IndirectClient> loadIdentityProviders() {
        return buildAllIdentityProviders(casProperties);
    }

    @Override
    public void destroy() {
        Optional.ofNullable(getCachedClients())
            .stream()
            .filter(SAML2Client.class::isInstance)
            .map(SAML2Client.class::cast)
            .forEach(Unchecked.consumer(SAML2Client::destroy));
    }
}
