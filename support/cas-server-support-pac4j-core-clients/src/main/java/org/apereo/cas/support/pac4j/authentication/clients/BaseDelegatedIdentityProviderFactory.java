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
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

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

    protected final Cache<String, List<BaseClient>> clientsCache;

    protected final ConfigurableApplicationContext applicationContext;

    private final CasReentrantLock lock = new CasReentrantLock();

    protected abstract List<BaseClient> load() throws Exception;

    @Override
    public void destroy() throws Exception {
        val clients = retrieve(casProperties.getServer().getName());
        Optional.ofNullable(clients)
            .stream()
            .filter(Closeable.class::isInstance)
            .map(Closeable.class::cast)
            .forEach(Unchecked.consumer(Closeable::close));
    }

    @Override
    public final List<BaseClient> build() {
        return lock.tryLock(() -> {
            val core = casProperties.getAuthn().getPac4j().getCore();
            val currentClients = !core.isLazyInit() || retrieve(casProperties.getServer().getName()).isEmpty()
                ? load()
                : retrieve(casProperties.getServer().getName());
            store(casProperties.getServer().getName(), currentClients);
            return currentClients;
        });
    }

    @Override
    public void store(final String key, final List<BaseClient> currentClients) {
        clientsCache.put(key, currentClients);
    }

    @Override
    public List<BaseClient> retrieve(final String key) {
        val cachedClients = clientsCache.getIfPresent(key);
        return ObjectUtils.getIfNull(cachedClients, new ArrayList<>());
    }

    @Override
    public List<BaseClient> rebuild() {
        clientsCache.invalidateAll();
        return build();
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

    @Override
    public List<BaseClient> buildFrom(final CasConfigurationProperties properties) throws Exception {
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

        return List.copyOf(newClients);
    }

    private List<ConfigurableDelegatedClientBuilder> getDelegatedClientBuilders() {
        val builders = new ArrayList<>(applicationContext.getBeansOfType(ConfigurableDelegatedClientBuilder.class).values());
        AnnotationAwareOrderComparator.sort(builders);
        return builders;
    }
}
