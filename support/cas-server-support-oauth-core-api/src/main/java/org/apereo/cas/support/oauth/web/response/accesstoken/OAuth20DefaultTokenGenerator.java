package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.CentralAuthenticationService;
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
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceToken;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCode;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenFactory;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;

/**
 * This is {@link OAuth20DefaultTokenGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Transactional(transactionManager = "ticketTransactionManager")
@Slf4j
@RequiredArgsConstructor
public class OAuth20DefaultTokenGenerator implements OAuth20TokenGenerator {
    /**
     * The Access token factory.
     */
    protected final OAuth20AccessTokenFactory accessTokenFactory;

    /**
     * The device token factory.
     */
    protected final OAuth20DeviceTokenFactory deviceTokenFactory;

    /**
     * The refresh token factory.
     */
    protected final OAuth20RefreshTokenFactory refreshTokenFactory;

    /**
     * The CAS service.
     */
    protected final CentralAuthenticationService centralAuthenticationService;

    /**
     * CAS configuration settings.
     */
    protected final CasConfigurationProperties casProperties;

    private static OAuth20TokenGeneratedResult generateAccessTokenResult(final AccessTokenRequestDataHolder holder,
                                                                         final Pair<OAuth20AccessToken, OAuth20RefreshToken> pair) {
        return OAuth20TokenGeneratedResult.builder()
            .registeredService(holder.getRegisteredService())
            .accessToken(pair.getKey())
            .refreshToken(pair.getValue())
            .grantType(holder.getGrantType())
            .responseType(holder.getResponseType())
            .build();
    }

    @Override
    public OAuth20TokenGeneratedResult generate(final AccessTokenRequestDataHolder holder) {
        if (OAuth20ResponseTypes.DEVICE_CODE.equals(holder.getResponseType())) {
            return generateAccessTokenOAuthDeviceCodeResponseType(holder);
        }

        val pair = generateAccessTokenOAuthGrantTypes(holder);
        return generateAccessTokenResult(holder, pair);
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
            val deviceCodeTicket = getDeviceTokenFromTicketRegistry(deviceCode);
            val deviceUserCode = getDeviceUserCodeFromRegistry(deviceCodeTicket);

            if (deviceUserCode.isUserCodeApproved()) {
                LOGGER.debug("Provided user code [{}] linked to device code [{}] is approved", deviceCodeTicket.getId(), deviceCode);
                this.centralAuthenticationService.deleteTicket(deviceCode);

                val deviceResult = AccessTokenRequestDataHolder.builder()
                    .service(holder.getService())
                    .authentication(holder.getAuthentication())
                    .registeredService(holder.getRegisteredService())
                    .ticketGrantingTicket(holder.getTicketGrantingTicket())
                    .grantType(holder.getGrantType())
                    .scopes(new LinkedHashSet<>(0))
                    .responseType(holder.getResponseType())
                    .generateRefreshToken(holder.getRegisteredService() != null && holder.isGenerateRefreshToken())
                    .build();

                val ticketPair = generateAccessTokenOAuthGrantTypes(deviceResult);
                return generateAccessTokenResult(deviceResult, ticketPair);
            }

            if (deviceCodeTicket.getLastTimeUsed() != null) {
                val interval = Beans.newDuration(casProperties.getAuthn().getOauth().getDeviceToken().getRefreshInterval()).getSeconds();
                val shouldSlowDown = deviceCodeTicket.getLastTimeUsed().plusSeconds(interval)
                    .isAfter(ZonedDateTime.now(ZoneOffset.UTC));
                if (shouldSlowDown) {
                    LOGGER.error("Request for user code approval is greater than the configured refresh interval of [{}] second(s)", interval);
                    throw new ThrottledOAuth20DeviceUserCodeApprovalException(deviceCodeTicket.getId());
                }
            }
            deviceCodeTicket.update();
            this.centralAuthenticationService.updateTicket(deviceCodeTicket);
            LOGGER.error("Provided user code [{}] linked to device code [{}] is NOT approved yet", deviceCodeTicket.getId(), deviceCode);
            throw new UnapprovedOAuth20DeviceUserCodeException(deviceCodeTicket.getId());
        }

        val deviceTokens = createDeviceTokensInTicketRegistry(holder);
        return OAuth20TokenGeneratedResult.builder()
            .responseType(holder.getResponseType())
            .registeredService(holder.getRegisteredService())
            .deviceCode(deviceTokens.getLeft().getId())
            .userCode(deviceTokens.getValue().getId())
            .build();
    }

    private OAuth20DeviceUserCode getDeviceUserCodeFromRegistry(final OAuth20DeviceToken deviceCodeTicket) {
        val userCode = this.centralAuthenticationService.getTicket(deviceCodeTicket.getUserCode(), OAuth20DeviceUserCode.class);
        if (userCode == null) {
            LOGGER.error("Provided user code [{}] is invalid or expired and cannot be found in the ticket registry", deviceCodeTicket.getUserCode());
            throw new InvalidOAuth20DeviceTokenException(deviceCodeTicket.getUserCode());
        }
        if (userCode.isExpired()) {
            this.centralAuthenticationService.deleteTicket(userCode.getId());
            LOGGER.error("Provided device code [{}] has expired and will be removed from the ticket registry", deviceCodeTicket.getUserCode());
            throw new InvalidOAuth20DeviceTokenException(deviceCodeTicket.getUserCode());
        }
        return userCode;
    }

    private OAuth20DeviceToken getDeviceTokenFromTicketRegistry(final String deviceCode) {
        try {
            val deviceCodeTicket = this.centralAuthenticationService.getTicket(deviceCode, OAuth20DeviceToken.class);
            if (deviceCodeTicket == null || deviceCodeTicket.isExpired()) {
                LOGGER.error("Provided device code [{}] is invalid or expired and cannot be found in the ticket registry", deviceCode);
                this.centralAuthenticationService.deleteTicket(deviceCode);
                throw new InvalidOAuth20DeviceTokenException(deviceCode);
            }
            return deviceCodeTicket;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new InvalidOAuth20DeviceTokenException(deviceCode);
        }
    }

    private Pair<OAuth20DeviceToken, OAuth20DeviceUserCode> createDeviceTokensInTicketRegistry(final AccessTokenRequestDataHolder holder) {
        val deviceToken = deviceTokenFactory.createDeviceCode(holder.getService());
        LOGGER.debug("Created device code token [{}]", deviceToken.getId());

        val deviceUserCode = deviceTokenFactory.createDeviceUserCode(deviceToken);
        LOGGER.debug("Created device user code token [{}]", deviceUserCode.getId());

        addTicketToRegistry(deviceToken);
        LOGGER.debug("Added device token [{}] to registry", deviceToken);

        addTicketToRegistry(deviceUserCode);
        LOGGER.debug("Added device user token [{}] to registry", deviceUserCode);

        return Pair.of(deviceToken, deviceUserCode);
    }

    /**
     * Generate access token OAuth grant types pair.
     *
     * @param holder the holder
     * @return the pair
     */
    protected Pair<OAuth20AccessToken, OAuth20RefreshToken> generateAccessTokenOAuthGrantTypes(final AccessTokenRequestDataHolder holder) {
        LOGGER.debug("Creating access token for [{}]", holder.getService());
        val clientId = holder.getRegisteredService().getClientId();
        val authn = DefaultAuthenticationBuilder
            .newInstance(holder.getAuthentication())
            .setAuthenticationDate(ZonedDateTime.now(ZoneOffset.UTC))
            .addAttribute(OAuth20Constants.GRANT_TYPE, holder.getGrantType().toString())
            .addAttribute(OAuth20Constants.SCOPE, holder.getScopes())
            .addAttribute(OAuth20Constants.CLIENT_ID, clientId)
            .addAttribute(OAuth20Constants.CLAIMS, holder.getClaims())
            .build();

        LOGGER.debug("Creating access token for [{}]", holder);
        val ticketGrantingTicket = holder.getTicketGrantingTicket();
        val accessToken = this.accessTokenFactory.create(holder.getService(),
            authn, ticketGrantingTicket, holder.getScopes(),
            clientId, holder.getClaims());

        LOGGER.debug("Created access token [{}]", accessToken);
        addTicketToRegistry(accessToken, ticketGrantingTicket);
        LOGGER.debug("Added access token [{}] to registry", accessToken);

        updateOAuthCode(holder, accessToken);

        val refreshToken = FunctionUtils.doIf(holder.isGenerateRefreshToken(),
            () -> generateRefreshToken(holder, accessToken),
            () -> {
                LOGGER.debug("Service [{}] is not able/allowed to receive refresh tokens", holder.getService());
                return null;
            }).get();

        return Pair.of(accessToken, refreshToken);
    }

    /**
     * Update OAuth code.
     *
     * @param holder      the holder
     * @param accessToken the accessToken
     */
    protected void updateOAuthCode(final AccessTokenRequestDataHolder holder, final OAuth20AccessToken accessToken) {
        if (holder.isRefreshToken()) {
            val refreshToken = (OAuth20RefreshToken) holder.getToken();
            refreshToken.getAccessTokens().add(accessToken.getId());
            this.centralAuthenticationService.updateTicket(refreshToken);
        } else if (holder.isCodeToken()) {
            val codeState = TicketState.class.cast(holder.getToken());
            codeState.update();

            if (holder.getToken().isExpired()) {
                this.centralAuthenticationService.deleteTicket(holder.getToken().getId());
            } else {
                this.centralAuthenticationService.updateTicket(holder.getToken());
            }
            this.centralAuthenticationService.updateTicket(holder.getTicketGrantingTicket());
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
        this.centralAuthenticationService.addTicket(ticket);
        if (ticketGrantingTicket != null) {
            LOGGER.debug("Updating parent ticket-granting ticket [{}]", ticketGrantingTicket);
            this.centralAuthenticationService.updateTicket(ticketGrantingTicket);
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
     * @param accessToken    the related Access token
     * @return the refresh token
     */
    protected OAuth20RefreshToken generateRefreshToken(final AccessTokenRequestDataHolder responseHolder, final OAuth20AccessToken accessToken) {
        LOGGER.debug("Creating refresh token for [{}]", responseHolder.getService());
        val refreshToken = this.refreshTokenFactory.create(responseHolder.getService(),
            responseHolder.getAuthentication(),
            responseHolder.getTicketGrantingTicket(),
            responseHolder.getScopes(),
            responseHolder.getRegisteredService().getClientId(),
            accessToken.getId(),
            responseHolder.getClaims());
        LOGGER.debug("Adding refresh token [{}] to the registry", refreshToken);
        addTicketToRegistry(refreshToken, responseHolder.getTicketGrantingTicket());
        if (responseHolder.isExpireOldRefreshToken()) {
            expireOldRefreshToken(responseHolder);
        }
        return refreshToken;
    }

    private void expireOldRefreshToken(final AccessTokenRequestDataHolder responseHolder) {
        val oldRefreshToken = responseHolder.getToken();
        LOGGER.debug("Expiring old refresh token [{}]", oldRefreshToken);
        oldRefreshToken.markTicketExpired();
        centralAuthenticationService.deleteTicket(oldRefreshToken);
    }
}
