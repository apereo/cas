package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.artifact.SamlArtifactTicket;
import org.apereo.cas.ticket.artifact.SamlArtifactTicketImpl;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicket;
import org.apereo.cas.ticket.query.SamlAttributeQueryTicketImpl;
import org.apereo.cas.util.spring.boot.ConditionalOnFeatureEnabled;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link SamlIdPTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.SAMLIdentityProvider)
@Configuration(value = "SamlIdPTicketCatalogConfiguration", proxyBeanMethods = false)
class SamlIdPTicketCatalogConfiguration extends BaseTicketCatalogConfigurer {

    @Override
    public void configureTicketCatalog(final TicketCatalog plan, final CasConfigurationProperties casProperties) {
        LOGGER.debug("Registering SAML2 protocol ticket definitions...");
        buildAndRegisterSamlArtifactDefinition(plan,
            buildTicketDefinition(plan, SamlArtifactTicket.PREFIX,
                SamlArtifactTicket.class, SamlArtifactTicketImpl.class,
                Ordered.HIGHEST_PRECEDENCE), casProperties);
        buildAndRegisterSamlAttributeQueryDefinition(plan,
            buildTicketDefinition(plan, SamlAttributeQueryTicket.PREFIX,
                SamlAttributeQueryTicket.class, SamlAttributeQueryTicketImpl.class,
                Ordered.HIGHEST_PRECEDENCE), casProperties);
    }

    protected void buildAndRegisterSamlArtifactDefinition(final TicketCatalog plan,
                                                          final TicketDefinition metadata,
                                                          final CasConfigurationProperties casProperties) {
        metadata.getProperties().setStorageName(casProperties.getAuthn().getSamlIdp().getTicket().getSamlArtifactsCacheStorageName());
        val timeToKillInSeconds = Beans.newDuration(casProperties.getTicket().getSt().getTimeToKillInSeconds()).toSeconds();
        metadata.getProperties().setStorageTimeout(timeToKillInSeconds);
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterSamlAttributeQueryDefinition(final TicketCatalog plan,
                                                                final TicketDefinition metadata,
                                                                final CasConfigurationProperties casProperties) {
        metadata.getProperties().setStorageName(casProperties.getAuthn().getSamlIdp().getTicket().getSamlAttributeQueryCacheStorageName());
        val timeToKillInSeconds = Beans.newDuration(casProperties.getTicket().getSt().getTimeToKillInSeconds()).toSeconds();
        metadata.getProperties().setStorageTimeout(timeToKillInSeconds);
        registerTicketDefinition(plan, metadata);
    }
}
