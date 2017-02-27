package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketMetadata;
import org.apereo.cas.ticket.TicketMetadataRegistrationPlan;
import org.apereo.cas.ticket.TicketMetadataRegistrationConfigurer;
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
 * This is {@link OAuthProtocolTicketMetadataRegistrationConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("oauthProtocolTicketMetadataRegistrationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class OAuthProtocolTicketMetadataRegistrationConfiguration implements TicketMetadataRegistrationConfigurer {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuthProtocolTicketMetadataRegistrationConfiguration.class);
    
    @Override
    public void configureTicketMetadataRegistrationPlan(final TicketMetadataRegistrationPlan plan) {
        LOGGER.debug("Registering core OAuth protocol ticket metadata types...");
        plan.registerTicketMetadata(new TicketMetadata(OAuthCodeImpl.class, OAuthCode.PREFIX));
        plan.registerTicketMetadata(new TicketMetadata(AccessTokenImpl.class, AccessToken.PREFIX));
        plan.registerTicketMetadata(new TicketMetadata(RefreshTokenImpl.class, RefreshToken.PREFIX));
    }
}
