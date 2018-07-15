package org.apereo.cas.support.oauth.web.response.accesstoken;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.validator.token.device.InvalidOAuth20DeviceTokenException;
import org.apereo.cas.support.oauth.validator.token.device.ThrottledOAuth20DeviceUserCodeApprovalException;
import org.apereo.cas.support.oauth.validator.token.device.UnapprovedOAuth20DeviceUserCodeException;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestDataHolder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.cas.ticket.accesstoken.AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.device.DeviceToken;
import org.apereo.cas.ticket.device.DeviceTokenFactory;
import org.apereo.cas.ticket.refreshtoken.RefreshToken;
import org.apereo.cas.ticket.refreshtoken.RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * This is {@link OAuth20DefaultTokenGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20DefaultTokenGenerator implements OAuth20TokenGenerator {
    /**
     * The Access token factory.
     */
    protected final AccessTokenFactory accessTokenFactory;

    /**
     * The device token factory.
     */
    protected final DeviceTokenFactory deviceTokenFactory;

    /**
     * The refresh token factory.
     */
    protected final RefreshTokenFactory refreshTokenFactory;

    /**
     * The Ticket registry.
     */
    protected final TicketRegistry ticketRegistry;

    /**
     * Cas configuration settings.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public OAuth20TokenGeneratedResult generate(final AccessTokenRequestDataHolder holder) {
        if (OAuth20ResponseTypes.DEVICE_CODE.equals(holder.getResponseType())) {
            return generateAccessTokenOAuthDeviceCodeResponseType(holder);
        }

        val pair = generateAccessTokenOAuthGrantTypes(holder);
        return OAuth20TokenGeneratedResult.builder()
            .registeredService(holder.getRegisteredService())
            .accessToken(pair.getKey())
            .refreshToken(pair.getValue())
            .grantType(holder.getGrantType())
            .build();
    }

    /**
     * Generate access token OAuth device code response type OAuth token generated result.
     *
     * @param holder the holder
     * @return the OAuth token generated result
     */
    protected OAuth20TokenGeneratedResult generateAccessTokenOAuthDeviceCodeResponseType(final AccessTokenRequestDataHolder holder) {
        val deviceCode = holder.getDeviceCode();

        if (StringUtils.isNotBlank(deviceCode)) {
            val deviceCodeTicket = this.ticketRegistry.getTicket(deviceCode, DeviceToken.class);
            if (deviceCodeTicket == null) {
                LOGGER.error("Provided device code [{}] is invalid or expired and cannot be found in the ticket registry", deviceCode);
                throw new InvalidOAuth20DeviceTokenException(deviceCode);
            }
            if (deviceCodeTicket.isExpired()) {
                this.ticketRegistry.deleteTicket(deviceCode);
                LOGGER.error("Provided device code [{}] has expired and will be removed from the ticket registry", deviceCode);
                throw new InvalidOAuth20DeviceTokenException(deviceCode);
            }
            if (deviceCodeTicket.isUserCodeApproved()) {
                LOGGER.debug("Provided user code [{}] linked to device code [{}] is approved", deviceCodeTicket.getUserCode(), deviceCode);
                this.ticketRegistry.deleteTicket(deviceCode);
                return OAuth20TokenGeneratedResult.builder()
                    .responseType(holder.getResponseType())
                    .registeredService(holder.getRegisteredService())
                    .deviceCode(deviceCode)
                    .build();
            }

            if (deviceCodeTicket.getLastTimeUsed() != null) {
                val interval = Beans.newDuration(casProperties.getAuthn().getOauth().getDeviceToken().getRefreshInterval()).getSeconds();
                val shouldSlowDown = deviceCodeTicket.getLastTimeUsed().plusSeconds(interval).isAfter(ZonedDateTime.now(ZoneOffset.UTC));
                if (shouldSlowDown) {
                    LOGGER.error("Request for user code approval is greater than the configured refresh interval of [{}] second(s)", interval);
                    throw new ThrottledOAuth20DeviceUserCodeApprovalException(deviceCodeTicket.getUserCode());
                }
            }
            deviceCodeTicket.update();
            this.ticketRegistry.updateTicket(deviceCodeTicket);
            LOGGER.error("Provided user code [{}] linked to device code [{}] is NOT approved yet", deviceCodeTicket.getUserCode(), deviceCode);
            throw new UnapprovedOAuth20DeviceUserCodeException(deviceCodeTicket.getUserCode());
        }

        val deviceToken = deviceTokenFactory.create(holder.getService());
        LOGGER.debug("Created device code token [{}]", deviceToken.getId());
        addTicketToRegistry(deviceToken);
        LOGGER.debug("Added device token [{}] to registry", deviceToken);

        return OAuth20TokenGeneratedResult.builder()
            .responseType(holder.getResponseType())
            .registeredService(holder.getRegisteredService())
            .deviceCode(deviceToken.getId())
            .userCode(deviceToken.getUserCode())
            .build();
    }

    /**
     * Generate access token OAuth grant types pair.
     *
     * @param holder the holder
     * @return the pair
     */
    protected Pair<AccessToken, RefreshToken> generateAccessTokenOAuthGrantTypes(final AccessTokenRequestDataHolder holder) {
        LOGGER.debug("Creating access token for [{}]", holder.getService());
        val authn = DefaultAuthenticationBuilder
            .newInstance(holder.getAuthentication())
            .addAttribute(OAuth20Constants.GRANT_TYPE, holder.getGrantType().toString())
            .build();

        LOGGER.debug("Creating access token for [{}]", holder);
        val accessToken = this.accessTokenFactory.create(holder.getService(),
            authn, holder.getTicketGrantingTicket(), holder.getScopes());

        LOGGER.debug("Created access token [{}]", accessToken);
        addTicketToRegistry(accessToken, holder.getTicketGrantingTicket());
        LOGGER.debug("Added access token [{}] to registry", accessToken);

        updateOAuthCode(holder);

        val refreshToken = FunctionUtils.doIf(holder.isGenerateRefreshToken(),
            () -> generateRefreshToken(holder),
            () -> {
                LOGGER.debug("Service [{}] is not able/allowed to receive refresh tokens", holder.getService());
                return null;
            }).get();

        return Pair.of(accessToken, refreshToken);
    }

    /**
     * Update OAuth code.
     *
     * @param holder the holder
     */
    protected void updateOAuthCode(final AccessTokenRequestDataHolder holder) {
        if (holder.getToken() instanceof OAuthCode) {
            val codeState = TicketState.class.cast(holder.getToken());
            codeState.update();

            if (holder.getToken().isExpired()) {
                this.ticketRegistry.deleteTicket(holder.getToken().getId());
            } else {
                this.ticketRegistry.updateTicket(holder.getToken());
            }
            this.ticketRegistry.updateTicket(holder.getTicketGrantingTicket());
        }
    }

    /**
     * Add ticket to registry.
     *
     * @param ticket               the ticket
     * @param ticketGrantingTicket the ticket granting ticket
     */
    protected void addTicketToRegistry(final Ticket ticket, final TicketGrantingTicket ticketGrantingTicket) {
        LOGGER.debug("Adding ticket [{}] to registry", ticket);
        this.ticketRegistry.addTicket(ticket);
        if (ticketGrantingTicket != null) {
            LOGGER.debug("Updating parent ticket-granting ticket [{}]", ticketGrantingTicket);
            this.ticketRegistry.updateTicket(ticketGrantingTicket);
        }
    }

    /**
     * Add ticket to registry.
     *
     * @param ticket the ticket
     */
    protected void addTicketToRegistry(final Ticket ticket) {
        addTicketToRegistry(ticket, null);
    }

    /**
     * Generate refresh token.
     *
     * @param responseHolder the response holder
     * @return the refresh token
     */
    protected RefreshToken generateRefreshToken(final AccessTokenRequestDataHolder responseHolder) {
        LOGGER.debug("Creating refresh token for [{}]", responseHolder.getService());
        val refreshToken = this.refreshTokenFactory.create(responseHolder.getService(),
            responseHolder.getAuthentication(), responseHolder.getTicketGrantingTicket(), responseHolder.getScopes());
        LOGGER.debug("Adding refresh token [{}] to the registry", refreshToken);
        addTicketToRegistry(refreshToken, responseHolder.getTicketGrantingTicket());
        return refreshToken;
    }
}
