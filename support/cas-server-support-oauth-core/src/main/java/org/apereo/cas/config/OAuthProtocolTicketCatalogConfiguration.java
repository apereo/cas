package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenImpl;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.code.OAuthCodeImpl;
import org.apereo.cas.ticket.device.DeviceToken;
import org.apereo.cas.ticket.device.DeviceTokenImpl;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenImpl;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This is {@link OAuthProtocolTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration("oauthProtocolTicketMetadataRegistrationConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class OAuthProtocolTicketCatalogConfiguration extends BaseTicketCatalogConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public void configureTicketCatalog(final TicketCatalog plan) {
        LOGGER.debug("Registering core OAuth protocol ticket definitions...");

        buildAndRegisterOAuthCodeDefinition(plan, buildTicketDefinition(plan, OAuthCode.PREFIX, OAuthCodeImpl.class));
        buildAndRegisterAccessTokenDefinition(plan, buildTicketDefinition(plan, AccessToken.PREFIX, AccessTokenImpl.class));
        buildAndRegisterRefreshTokenDefinition(plan, buildTicketDefinition(plan, RefreshToken.PREFIX, RefreshTokenImpl.class));
        buildAndRegisterDeviceTokenDefinition(plan, buildTicketDefinition(plan, DeviceToken.PREFIX, DeviceTokenImpl.class));
    }

    private void buildAndRegisterDeviceTokenDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("oauthDeviceTokensCache");
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getDeviceToken().getMaxTimeToLiveInSeconds()).getSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterAccessTokenDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("oauthAccessTokensCache");
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getAccessToken().getMaxTimeToLiveInSeconds()).getSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterRefreshTokenDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("oauthRefreshTokensCache");
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getRefreshToken().getTimeToKillInSeconds()).getSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterOAuthCodeDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("oauthCodesCache");
        metadata.getProperties().setStorageTimeout(casProperties.getAuthn().getOauth().getCode().getTimeToKillInSeconds());
        registerTicketDefinition(plan, metadata);
    }
}
