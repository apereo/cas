package org.apereo.cas.config;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketImpl;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link SamlIdPTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Configuration("samlIdPTicketCatalogConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class SamlIdPTicketCatalogConfiguration extends BaseTicketCatalogConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public void configureTicketCatalog(final TicketCatalog plan) {
        LOGGER.debug("Registering SAML2 protocol ticket definitions...");
        buildAndRegisterSamlArtifactDefinition(plan, buildTicketDefinition(plan, SamlArtifactTicket.PREFIX, SamlArtifactTicketImpl.class));
        buildAndRegisterSamlAttributeQueryDefinition(plan, buildTicketDefinition(plan, SamlAttributeQueryTicket.PREFIX, SamlArtifactTicketImpl.class));
    }

    protected void buildAndRegisterSamlArtifactDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName(casProperties.getAuthn().getSamlIdp().getTicket().getSamlArtifactsCacheStorageName());
        metadata.getProperties().setStorageTimeout(casProperties.getTicket().getSt().getTimeToKillInSeconds());
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterSamlAttributeQueryDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName(casProperties.getAuthn().getSamlIdp().getTicket().getSamlAttributeQueryCacheStorageName());
        metadata.getProperties().setStorageTimeout(casProperties.getTicket().getSt().getTimeToKillInSeconds());
        registerTicketDefinition(plan, metadata);
    }
}
