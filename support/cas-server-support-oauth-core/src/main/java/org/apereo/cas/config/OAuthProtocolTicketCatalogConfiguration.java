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
import org.apereo.cas.ticket.device.DeviceUserCode;
import org.apereo.cas.ticket.device.DeviceUserCodeImpl;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenImpl;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuthProtocolTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "oauthProtocolTicketMetadataRegistrationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class OAuthProtocolTicketCatalogConfiguration extends BaseTicketCatalogConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public void configureTicketCatalog(final TicketCatalog plan) {
        LOGGER.trace("Registering core OAuth protocol ticket definitions...");

        buildAndRegisterOAuthCodeDefinition(plan, buildTicketDefinition(plan, OAuthCode.PREFIX, OAuthCodeImpl.class, Ordered.HIGHEST_PRECEDENCE));
        buildAndRegisterAccessTokenDefinition(plan, buildTicketDefinition(plan, AccessToken.PREFIX, AccessTokenImpl.class, Ordered.HIGHEST_PRECEDENCE));
        buildAndRegisterRefreshTokenDefinition(plan, buildTicketDefinition(plan, RefreshToken.PREFIX, RefreshTokenImpl.class, Ordered.HIGHEST_PRECEDENCE));
        buildAndRegisterDeviceTokenDefinition(plan, buildTicketDefinition(plan, DeviceToken.PREFIX, DeviceTokenImpl.class));
        buildAndRegisterDeviceUserCodeDefinition(plan, buildTicketDefinition(plan, DeviceUserCode.PREFIX, DeviceUserCodeImpl.class));
    }

    private void buildAndRegisterDeviceTokenDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("oauthDeviceTokensCache");
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getDeviceToken().getMaxTimeToLiveInSeconds()).getSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        registerTicketDefinition(plan, metadata);
    }

    private void buildAndRegisterDeviceUserCodeDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("oauthDeviceUserCodesCache");
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getDeviceUserCode().getMaxTimeToLiveInSeconds()).getSeconds();
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
