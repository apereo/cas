package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.IndirectClient;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.store.SAMLMessageStoreFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ObjectProvider;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link DefaultDelegatedClientFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
public class DefaultDelegatedClientFactory extends BaseDelegatedClientFactory implements DisposableBean {

    private final CasConfigurationProperties casProperties;

    private final Set<IndirectClient> clients = Collections.synchronizedSet(new LinkedHashSet<>());

    public DefaultDelegatedClientFactory(
        final CasConfigurationProperties casProperties,
        final Collection<DelegatedClientFactoryCustomizer> customizers,
        final CasSSLContext casSSLContext,
        final ObjectProvider<SAMLMessageStoreFactory> samlMessageStoreFactory) {
        super(customizers, casSSLContext, samlMessageStoreFactory);
        this.casProperties = casProperties;
    }

    @Override
    @Synchronized
    public Collection<IndirectClient> build() {
        if (clients.isEmpty() || !casProperties.getAuthn().getPac4j().getCore().isLazyInit()) {
            clients.addAll(buildAllIdentityProviders(casProperties));
        }
        return clients;
    }

    @Override
    public void destroy() {
        clients
            .stream()
            .filter(client -> client instanceof SAML2Client)
            .map(SAML2Client.class::cast)
            .forEach(Unchecked.consumer(SAML2Client::destroy));
    }
}
