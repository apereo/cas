package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.DefaultTicketDefinition;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.TicketMetadataCatalogConfigurer;
import org.apereo.cas.ticket.TicketMetadataCatalog;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenImpl;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeImpl;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link OAuthProtocolTicketMetadataCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("oauthProtocolTicketMetadataRegistrationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OAuthProtocolTicketMetadataCatalogConfiguration implements TicketMetadataCatalogConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthProtocolTicketMetadataCatalogConfiguration.class);

    @Override
    public void configureTicketMetadataCatalog(final TicketMetadataCatalog plan) {
        LOGGER.debug("Registering core OAuth protocol ticket metadata types...");

        buildAndRegisterOAuthCodeMetadata(plan, buildTicketMetadata(plan, OAuthCode.PREFIX, OAuthCodeImpl.class));
        buildAndRegisterAccessTokenMetadata(plan, buildTicketMetadata(plan, AccessToken.PREFIX, AccessTokenImpl.class));
        buildAndRegisterRefreshTokenMetadata(plan, buildTicketMetadata(plan, RefreshToken.PREFIX, RefreshTokenImpl.class));
    }

    protected void buildAndRegisterAccessTokenMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        registerTicketMetadata(plan, metadata);
    }

    protected void buildAndRegisterRefreshTokenMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        registerTicketMetadata(plan, metadata);
    }

    protected void buildAndRegisterOAuthCodeMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        registerTicketMetadata(plan, metadata);
    }

    private TicketDefinition buildTicketMetadata(final TicketMetadataCatalog plan, final String prefix, final Class impl) {
        if (plan.containsTicketMetadata(prefix)) {
            return plan.findTicketMetadata(prefix);
        }
        return new DefaultTicketDefinition(impl, prefix);
    }

    /**
     * Register ticket metadata.
     *
     * @param plan     the plan
     * @param metadata the metadata
     */
    private void registerTicketMetadata(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        plan.registerTicketMetadata(metadata);
    }

}
