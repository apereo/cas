package org.apereo.cas.ticket;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
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
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20TicketCatalogConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class OAuth20TicketCatalogConfigurer extends BaseTicketCatalogConfigurer {

    @Override
    public void configureTicketCatalog(final TicketCatalog plan,
                                       final CasConfigurationProperties casProperties) throws Throwable {
        LOGGER.trace("Registering OAuth protocol ticket definitions...");
        buildAndRegisterOAuthCodeDefinition(plan, buildTicketDefinition(plan, OAuth20Code.PREFIX,
            OAuth20Code.class, OAuth20DefaultCode.class, Ordered.HIGHEST_PRECEDENCE), casProperties);
        buildAndRegisterAccessTokenDefinition(plan, buildTicketDefinition(plan,
            OAuth20AccessToken.PREFIX, OAuth20AccessToken.class, OAuth20DefaultAccessToken.class,
            Ordered.HIGHEST_PRECEDENCE), casProperties);
        buildAndRegisterRefreshTokenDefinition(plan, buildTicketDefinition(plan, OAuth20RefreshToken.PREFIX,
            OAuth20RefreshToken.class, OAuth20DefaultRefreshToken.class, Ordered.HIGHEST_PRECEDENCE), casProperties);
        buildAndRegisterDeviceTokenDefinition(plan, buildTicketDefinition(plan, OAuth20DeviceToken.PREFIX,
            OAuth20DefaultDeviceToken.class, OAuth20DeviceToken.class), casProperties);
        buildAndRegisterDeviceUserCodeDefinition(plan, buildTicketDefinition(plan, OAuth20DeviceUserCode.PREFIX,
            OAuth20DefaultDeviceUserCode.class, OAuth20DeviceUserCode.class), casProperties);
    }

    protected void buildAndRegisterAccessTokenDefinition(final TicketCatalog plan, final TicketDefinition metadata,
                                                         final CasConfigurationProperties casProperties) {
        metadata.getProperties().setStorageName(casProperties.getAuthn().getOauth().getAccessToken().getStorageName());
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getAccessToken().getMaxTimeToLiveInSeconds()).toSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        metadata.getProperties().setExcludeFromCascade(casProperties.getTicket().isTrackDescendantTickets());
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterRefreshTokenDefinition(final TicketCatalog plan, final TicketDefinition metadata,
                                                          final CasConfigurationProperties casProperties) {
        metadata.getProperties().setStorageName(casProperties.getAuthn().getOauth().getRefreshToken().getStorageName());
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getRefreshToken().getTimeToKillInSeconds()).toSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        metadata.getProperties().setExcludeFromCascade(casProperties.getTicket().isTrackDescendantTickets());
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterOAuthCodeDefinition(final TicketCatalog plan, final TicketDefinition metadata,
                                                       final CasConfigurationProperties casProperties) {
        metadata.getProperties().setStorageName(casProperties.getAuthn().getOauth().getCode().getStorageName());
        metadata.getProperties().setStorageTimeout(casProperties.getAuthn().getOauth().getCode().getTimeToKillInSeconds());
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterDeviceTokenDefinition(final TicketCatalog plan,
                                                         final TicketDefinition metadata,
                                                         final CasConfigurationProperties casProperties) {
        metadata.getProperties().setStorageName(casProperties.getAuthn().getOauth().getDeviceToken().getStorageName());
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getDeviceToken().getMaxTimeToLiveInSeconds()).toSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        metadata.getProperties().setExcludeFromCascade(true);
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterDeviceUserCodeDefinition(final TicketCatalog plan, final TicketDefinition metadata,
                                                            final CasConfigurationProperties casProperties) {
        metadata.getProperties().setStorageName(casProperties.getAuthn().getOauth().getDeviceUserCode().getStorageName());
        val timeout = Beans.newDuration(casProperties.getAuthn().getOauth().getDeviceUserCode().getMaxTimeToLiveInSeconds()).toSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        metadata.getProperties().setExcludeFromCascade(true);
        registerTicketDefinition(plan, metadata);
    }
}
