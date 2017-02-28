package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.TicketMetadata;
import org.apereo.cas.ticket.TicketMetadataRegistrationConfigurer;
import org.apereo.cas.ticket.TicketMetadataRegistrationPlan;
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

        buildAndRegisterOAuthCodeMetadata(plan, buildTicketMetadata(plan, OAuthCode.PREFIX, OAuthCodeImpl.class));
        buildAndRegisterAccessTokenMetadata(plan, buildTicketMetadata(plan, AccessToken.PREFIX, AccessTokenImpl.class));
        buildAndRegisterRefreshTokenMetadata(plan, buildTicketMetadata(plan, RefreshToken.PREFIX, RefreshTokenImpl.class));
    }

    protected void buildAndRegisterAccessTokenMetadata(final TicketMetadataRegistrationPlan plan, final TicketMetadata metadata) {
        registerTicketMetadata(plan, metadata);
    }

    protected void buildAndRegisterRefreshTokenMetadata(final TicketMetadataRegistrationPlan plan, final TicketMetadata metadata) {
        registerTicketMetadata(plan, metadata);
    }

    protected void buildAndRegisterOAuthCodeMetadata(final TicketMetadataRegistrationPlan plan, final TicketMetadata metadata) {
        registerTicketMetadata(plan, metadata);
    }

    private TicketMetadata buildTicketMetadata(final TicketMetadataRegistrationPlan plan, final String prefix, final Class impl) {
        if (plan.containsTicketMetadata(prefix)) {
            return plan.findTicketMetadata(prefix);
        }
        return new TicketMetadata(impl, prefix);
    }

    /**
     * Register ticket metadata.
     *
     * @param plan     the plan
     * @param metadata the metadata
     */
    private void registerTicketMetadata(final TicketMetadataRegistrationPlan plan, final TicketMetadata metadata) {
        plan.registerTicketMetadata(metadata);
    }

}
