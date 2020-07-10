package org.apereo.cas.config;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessToken;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.code.OAuth20DefaultCode;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceToken;
import org.apereo.cas.ticket.device.OAuth20DefaultDeviceUserCode;
import org.apereo.cas.ticket.device.OAuth20DeviceToken;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCode;
import org.apereo.cas.ticket.refreshtoken.OAuth20DefaultRefreshToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20ProtocolTicketCatalogConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Configuration(value = "oauthProtocolTicketMetadataRegistrationConfiguration", proxyBeanMethods = false)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@Slf4j
public class OAuth20ProtocolTicketCatalogConfiguration extends BaseTicketCatalogConfigurer {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public void configureTicketCatalog(final TicketCatalog plan) {
        LOGGER.trace("Registering core OAuth protocol ticket definitions...");

        buildAndRegisterOAuthCodeDefinition(plan,
            buildTicketDefinition(plan, OAuth20Code.PREFIX, OAuth20DefaultCode.class, Ordered.HIGHEST_PRECEDENCE));
        buildAndRegisterAccessTokenDefinition(plan,
            buildTicketDefinition(plan, OAuth20AccessToken.PREFIX, OAuth20DefaultAccessToken.class, Ordered.HIGHEST_PRECEDENCE));
        buildAndRegisterRefreshTokenDefinition(plan,
            buildTicketDefinition(plan, OAuth20RefreshToken.PREFIX, OAuth20DefaultRefreshToken.class, Ordered.HIGHEST_PRECEDENCE));
        buildAndRegisterDeviceTokenDefinition(plan,
            buildTicketDefinition(plan, OAuth20DeviceToken.PREFIX, OAuth20DefaultDeviceToken.class));
        buildAndRegisterDeviceUserCodeDefinition(plan,
            buildTicketDefinition(plan, OAuth20DeviceUserCode.PREFIX, OAuth20DefaultDeviceUserCode.class));
    }

    private void buildAndRegisterDeviceTokenDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("oauthDeviceTokensCache");
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getDeviceToken().getMaxTimeToLiveInSeconds()).getSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        metadata.getProperties().setExcludeFromCascade(true);
        registerTicketDefinition(plan, metadata);
    }

    private void buildAndRegisterDeviceUserCodeDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("oauthDeviceUserCodesCache");
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getDeviceUserCode().getMaxTimeToLiveInSeconds()).getSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        metadata.getProperties().setExcludeFromCascade(true);
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterAccessTokenDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("oauthAccessTokensCache");
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getAccessToken().getMaxTimeToLiveInSeconds()).getSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        metadata.getProperties().setExcludeFromCascade(casProperties.getLogout().isRemoveDescendantTickets());
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterRefreshTokenDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("oauthRefreshTokensCache");
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getRefreshToken().getTimeToKillInSeconds()).getSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        metadata.getProperties().setExcludeFromCascade(casProperties.getLogout().isRemoveDescendantTickets());
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterOAuthCodeDefinition(final TicketCatalog plan, final TicketDefinition metadata) {
        metadata.getProperties().setStorageName("oauthCodesCache");
        metadata.getProperties().setStorageTimeout(casProperties.getAuthn().getOauth().getCode().getTimeToKillInSeconds());
        registerTicketDefinition(plan, metadata);
    }
}
