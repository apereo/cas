package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Contains common implementations of ticket definition builders shared across various
 * ticket registries catalog configurations.
 *
 * @author Dmitriy Kopylenko
 * @since 6.1.0
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseTicketDefinitionBuilderSupportConfiguration extends CasCoreTicketCatalogConfiguration {

    private final CasConfigurationProperties casProperties;

    private final CasTicketCatalogConfigurationValuesProvider configurationValuesProvider;

    private final ConfigurableApplicationContext applicationContext;
    
    @Override
    protected void buildAndRegisterServiceTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName(configurationValuesProvider.getServiceTicketStorageName().apply(casProperties));
        metadata.getProperties().setStorageTimeout(configurationValuesProvider.getServiceTicketStorageTimeout().apply(applicationContext));
        super.buildAndRegisterServiceTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName(configurationValuesProvider.getProxyTicketStorageName().apply(casProperties));
        metadata.getProperties().setStorageTimeout(configurationValuesProvider.getProxyTicketStorageTimeout().apply(applicationContext));
        super.buildAndRegisterProxyTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterTicketGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName(configurationValuesProvider.getTicketGrantingTicketStorageName().apply(casProperties));
        metadata.getProperties().setStorageTimeout(configurationValuesProvider.getTicketGrantingTicketStorageTimeout().apply(applicationContext));
        super.buildAndRegisterTicketGrantingTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterProxyGrantingTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName(configurationValuesProvider.getProxyGrantingTicketStorageName().apply(casProperties));
        metadata.getProperties().setStorageTimeout(configurationValuesProvider.getProxyGrantingTicketStorageTimeout().apply(applicationContext));
        super.buildAndRegisterProxyGrantingTicketDefinition(plan, metadata);
    }

    @Override
    protected void buildAndRegisterTransientSessionTicketDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName(configurationValuesProvider.getTransientSessionStorageName().apply(casProperties));
        metadata.getProperties().setStorageTimeout(configurationValuesProvider.getTransientSessionStorageTimeout().apply(applicationContext));
        super.buildAndRegisterTransientSessionTicketDefinition(plan, metadata);
    }
}
