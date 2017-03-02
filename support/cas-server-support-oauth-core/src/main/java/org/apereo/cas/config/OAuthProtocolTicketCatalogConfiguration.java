package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.TicketDefinition;
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
import org.springframework.core.Ordered;

/**
 * This is {@link OAuthProtocolTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("oauthProtocolTicketMetadataRegistrationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OAuthProtocolTicketCatalogConfiguration extends BaseTicketCatalogConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthProtocolTicketCatalogConfiguration.class);

    @Override
    public void configureTicketCatalog(final TicketMetadataCatalog plan) {
        LOGGER.debug("Registering core OAuth protocol ticket metadata types...");

        buildAndRegisterOAuthCodeDefinition(plan, buildTicketDefinition(plan, OAuthCode.PREFIX, OAuthCodeImpl.class, Ordered.LOWEST_PRECEDENCE));
        buildAndRegisterAccessTokenDefinition(plan, buildTicketDefinition(plan, AccessToken.PREFIX, AccessTokenImpl.class, Ordered.LOWEST_PRECEDENCE));
        buildAndRegisterRefreshTokenDefinition(plan, buildTicketDefinition(plan, RefreshToken.PREFIX, RefreshTokenImpl.class, Ordered.LOWEST_PRECEDENCE));
    }

    protected void buildAndRegisterAccessTokenDefinition(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterRefreshTokenDefinition(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterOAuthCodeDefinition(final TicketMetadataCatalog plan, final TicketDefinition metadata) {
        registerTicketDefinition(plan, metadata);
    }
}
