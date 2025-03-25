package org.apereo.cas.support.pac4j.authentication.clients;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.pac4j.Pac4jBaseClientProperties;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.ObjectUtils;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.client.BaseClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This is {@link BaseDelegatedIdentityProviderFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseDelegatedIdentityProviderFactory implements DelegatedIdentityProviderFactory {

    protected final CasConfigurationProperties casProperties;

    protected final Collection<DelegatedClientFactoryCustomizer> customizers;

    protected final CasSSLContext casSSLContext;

    protected final Cache<String, Collection<BaseClient>> clientsCache;

    protected final ConfigurableApplicationContext applicationContext;
    
    private final CasReentrantLock lock = new CasReentrantLock();

    protected abstract Collection<BaseClient> loadIdentityProviders() throws Exception;


    @Override
    public final Collection<BaseClient> build() {
        return lock.tryLock(() -> {
            val core = casProperties.getAuthn().getPac4j().getCore();
            val currentClients = getCachedClients().isEmpty() || !core.isLazyInit() ? loadIdentityProviders() : getCachedClients();
            clientsCache.put(casProperties.getServer().getName(), currentClients);
            return currentClients;
        });
    }

    @Override
    public Collection<BaseClient> rebuild() {
        clientsCache.invalidateAll();
        return build();
    }

    protected Collection<BaseClient> getCachedClients() {
        val cachedClients = clientsCache.getIfPresent(casProperties.getServer().getName());
        return ObjectUtils.defaultIfNull(cachedClients, new ArrayList<>());
    }

    protected BaseClient configureClient(final BaseClient client,
                                         final Pac4jBaseClientProperties clientProperties,
                                         final CasConfigurationProperties givenProperties) {
        if (clientProperties != null) {
            DelegatedIdentityProviderFactory.configureClientName(client, clientProperties.getClientName());
            DelegatedIdentityProviderFactory.configureClientCustomProperties(client, clientProperties);
            DelegatedIdentityProviderFactory.configureClientCallbackUrl(client, clientProperties, casProperties.getServer().getLoginUrl());
        }

        invokeClientCustomizers(client);

        if (!givenProperties.getAuthn().getPac4j().getCore().isLazyInit()) {
            client.init();
        }
        LOGGER.debug("Configured external identity provider [{}]", client.getName());
        return client;
    }
    

    protected void invokeClientCustomizers(final BaseClient client) {
        customizers.forEach(customizer -> customizer.customize(client));
    }

    protected Set<BaseClient> buildAllIdentityProviders(final CasConfigurationProperties properties) throws Exception {
        val newClients = new LinkedHashSet<BaseClient>();
        val builders = getDelegatedClientBuilders();
        for (val builder : builders) {
            val builtClients = builder.build(properties);
            LOGGER.debug("Builder [{}] provides [{}] clients", builder.getName(), builtClients.size());
            builtClients.forEach(Unchecked.consumer(instance -> {
                val preparedClient = configureClient(instance.getClient(), instance.getProperties(), properties);
                val configuredClients = builder.configure(preparedClient, instance.getProperties(), properties);
                configuredClients
                    .stream()
                    .filter(configured -> configured.equals(preparedClient))
                    .forEach(this::invokeClientCustomizers);
                newClients.addAll(configuredClients);
            }));
        }

        return newClients;
    }

    private List<ConfigurableDelegatedClientBuilder> getDelegatedClientBuilders() {
        val builders = new ArrayList<>(applicationContext.getBeansOfType(ConfigurableDelegatedClientBuilder.class).values());
        AnnotationAwareOrderComparator.sort(builders);
        return builders;
    }
}
