package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.token.device.InvalidOAuth20DeviceTokenException;
import org.apereo.cas.support.oauth.validator.token.device.ThrottledOAuth20DeviceUserCodeApprovalException;
import org.apereo.cas.support.oauth.validator.token.device.UnapprovedOAuth20DeviceUserCodeException;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceToken;
import org.apereo.cas.ticket.device.OAuth20DeviceTokenFactory;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCode;
import org.apereo.cas.ticket.device.OAuth20DeviceUserCodeFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshTokenFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jooq.lambda.Unchecked;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;

/**
 * This is {@link OAuth20DefaultTokenGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20DefaultTokenGenerator implements OAuth20TokenGenerator {
    protected final TicketFactory ticketFactory;

    protected final TicketRegistry ticketRegistry;

    protected final PrincipalResolver principalResolver;

    protected final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    protected final CasConfigurationProperties casProperties;

    private static OAuth20TokenGeneratedResult generateAccessTokenResult(
        final AccessTokenRequestContext tokenRequestContext,
        final AccessAndRefreshTokens accessAndRefreshTokens) {
        return OAuth20TokenGeneratedResult
            .builder()
            .registeredService(tokenRequestContext.getRegisteredService())
            .accessToken(accessAndRefreshTokens.accessToken())
            .refreshToken(accessAndRefreshTokens.refreshToken())
            .grantType(tokenRequestContext.getGrantType())
            .responseType(tokenRequestContext.getResponseType())
            .build();
    }

    @Override
    public OAuth20TokenGeneratedResult generate(final AccessTokenRequestContext tokenRequestContext) throws Throwable {
        if (OAuth20ResponseTypes.DEVICE_CODE == tokenRequestContext.getResponseType()) {
            return generateAccessTokenOAuthDeviceCodeResponseType(tokenRequestContext);
        }
        val accessAndRefreshTokens = generateAccessTokenOAuthGrantTypes(tokenRequestContext);
        return generateAccessTokenResult(tokenRequestContext, accessAndRefreshTokens);
    }

    protected OAuth20TokenGeneratedResult generateAccessTokenOAuthDeviceCodeResponseType(
        final AccessTokenRequestContext tokenRequestContext) throws Throwable {
        val deviceCode = tokenRequestContext.getDeviceCode();

        if (StringUtils.isNotBlank(deviceCode)) {
            val deviceCodeTicket = getDeviceTokenFromTicketRegistry(deviceCode);
            val deviceUserCode = getDeviceUserCodeFromRegistry(deviceCodeTicket);

            if (deviceUserCode.isUserCodeApproved()) {
                LOGGER.debug("Provided user code [{}] linked to device code [{}] is approved", deviceCodeTicket.getId(), deviceCode);
                ticketRegistry.deleteTicket(deviceCode);

                val deviceResult = AccessTokenRequestContext
                    .builder()
                    .service(tokenRequestContext.getService())
                    .authentication(tokenRequestContext.getAuthentication())
                    .registeredService(tokenRequestContext.getRegisteredService())
                    .ticketGrantingTicket(tokenRequestContext.getTicketGrantingTicket())
                    .grantType(tokenRequestContext.getGrantType())
                    .scopes(new LinkedHashSet<>())
                    .responseType(tokenRequestContext.getResponseType())
                    .generateRefreshToken(tokenRequestContext.getRegisteredService() != null && tokenRequestContext.isGenerateRefreshToken())
                    .build();

                val ticketPair = generateAccessTokenOAuthGrantTypes(deviceResult);
                return generateAccessTokenResult(deviceResult, ticketPair);
            }

            if (deviceCodeTicket.getLastTimeUsed() != null) {
                val interval = Beans.newDuration(casProperties.getAuthn().getOauth().getDeviceToken().getRefreshInterval()).toSeconds();
                val shouldSlowDown = deviceCodeTicket.getLastTimeUsed().plusSeconds(interval)
                    .isAfter(ZonedDateTime.now(ZoneOffset.UTC));
                if (shouldSlowDown) {
                    LOGGER.error("Request for user code approval is greater than the configured refresh interval of [{}] second(s)", interval);
                    throw new ThrottledOAuth20DeviceUserCodeApprovalException(deviceCodeTicket.getId());
                }
            }
            deviceCodeTicket.update();
            ticketRegistry.updateTicket(deviceCodeTicket);
            LOGGER.error("Provided user code [{}] linked to device code [{}] is NOT approved yet", deviceCodeTicket.getId(), deviceCode);
            throw new UnapprovedOAuth20DeviceUserCodeException(deviceCodeTicket.getId());
        }

        val deviceTokens = createDeviceTokensInTicketRegistry(tokenRequestContext);
        return OAuth20TokenGeneratedResult
            .builder()
            .responseType(tokenRequestContext.getResponseType())
            .registeredService(tokenRequestContext.getRegisteredService())
            .deviceCode(deviceTokens.deviceCode().getId())
            .userCode(deviceTokens.userCode().getId())
            .build();
    }

    protected AuthenticationBuilder prepareAuthentication(final AccessTokenRequestContext tokenRequestContext) {
        val ticketGrantingTicket = tokenRequestContext.getTicketGrantingTicket();
        var existingAuthn = tokenRequestContext.getAuthentication();
        if (existingAuthn == null && ticketGrantingTicket instanceof final AuthenticationAwareTicket aat) {
            existingAuthn = aat.getAuthentication();
        }
        val authnBuilder = DefaultAuthenticationBuilder
            .newInstance(existingAuthn)
            .setAuthenticationDate(ZonedDateTime.now(ZoneOffset.UTC))
            .addAttribute(OAuth20Constants.GRANT_TYPE, tokenRequestContext.getGrantType().getType())
            .addAttribute(OAuth20Constants.SCOPE, tokenRequestContext.getScopes());

        val requestedClaims = tokenRequestContext.getClaims().getOrDefault(OAuth20Constants.CLAIMS_USERINFO, new HashMap<>());
        requestedClaims.forEach(authnBuilder::addAttribute);

        FunctionUtils.doIfNotNull(tokenRequestContext.getDpop(),
            __ -> authnBuilder.addAttribute(OAuth20Constants.DPOP, tokenRequestContext.getDpop()));
        FunctionUtils.doIfNotNull(tokenRequestContext.getDpopConfirmation(),
            __ -> authnBuilder.addAttribute(OAuth20Constants.DPOP_CONFIRMATION, tokenRequestContext.getDpopConfirmation()));
        return authnBuilder;
    }

    protected AccessAndRefreshTokens generateAccessTokenOAuthGrantTypes(
        final AccessTokenRequestContext tokenRequestContext) throws Throwable {
        LOGGER.debug("Creating access token for [{}]", tokenRequestContext.getService());

        if (tokenRequestContext.getGrantType() == OAuth20GrantTypes.TOKEN_EXCHANGE) {
            return generateAccessTokenForTokenExchange(tokenRequestContext);
        }

        val authentication = finalizeAuthentication(tokenRequestContext, prepareAuthentication(tokenRequestContext));
        LOGGER.debug("Creating access token for [{}]", tokenRequestContext);
        val accessToken = createAccessToken(tokenRequestContext, authentication);
        val addedAccessToken = addAccessToken(tokenRequestContext, accessToken);
        val refreshToken = FunctionUtils.doIf(tokenRequestContext.isGenerateRefreshToken(),
            Unchecked.supplier(() -> generateRefreshToken(tokenRequestContext, accessToken.getId())),
            () -> {
                LOGGER.debug("Service [{}] is not able/allowed to receive refresh tokens", tokenRequestContext.getService());
                return null;
            }).get();
        return new AccessAndRefreshTokens(addedAccessToken, refreshToken);
    }

    protected Authentication finalizeAuthentication(final AccessTokenRequestContext tokenRequestContext,
                                                    final AuthenticationBuilder authenticationBuilder) {
        return authenticationBuilder.build();
    }

    protected AccessAndRefreshTokens generateAccessTokenForTokenExchange(final AccessTokenRequestContext tokenRequestContext) throws Throwable {
        val targetService = Objects.requireNonNullElseGet(tokenRequestContext.getTokenExchangeResource(), tokenRequestContext::getService);
        return switch (tokenRequestContext.getRequestedTokenType()) {
            case ACCESS_TOKEN, JWT -> {
                val subjectToken = (OAuth20AccessToken) tokenRequestContext.getSubjectToken();
                val exchangedAccessToken = exchangeTokenForAccessToken(targetService, subjectToken, tokenRequestContext);
                val addedAccessToken = addAccessToken(tokenRequestContext, exchangedAccessToken);
                yield new AccessAndRefreshTokens(addedAccessToken, null);
            }
            default -> {
                val subjectToken = (OAuth20AccessToken) tokenRequestContext.getSubjectToken();
                val exchangedAccessToken = exchangeTokenForAccessToken(targetService, subjectToken, tokenRequestContext);
                yield new AccessAndRefreshTokens(exchangedAccessToken, null);
            }
        };
    }

    private OAuth20AccessToken createAccessToken(final AccessTokenRequestContext tokenRequestContext,
                                                 final Authentication authentication) throws Throwable {
        val clientId = Optional.ofNullable(tokenRequestContext.getRegisteredService())
            .map(OAuthRegisteredService::getClientId).orElse(StringUtils.EMPTY);

        val accessTokenFactory = (OAuth20AccessTokenFactory) ticketFactory.get(OAuth20AccessToken.class);
        val ticketGrantingTicket = tokenRequestContext.getTicketGrantingTicket() == null || tokenRequestContext.getTicketGrantingTicket().isExpired()
            ? null : tokenRequestContext.getTicketGrantingTicket();
        LOGGER.debug("Creating access token for client id [{}] and authentication [{}]", clientId, authentication);
        return accessTokenFactory.create(tokenRequestContext.getService(),
            authentication,
            ticketGrantingTicket,
            tokenRequestContext.getScopes(),
            Optional.ofNullable(tokenRequestContext.getToken()).map(Ticket::getId).orElse(null),
            clientId,
            tokenRequestContext.getClaims(),
            tokenRequestContext.getResponseType(),
            tokenRequestContext.getGrantType());
    }

    private OAuth20AccessToken exchangeTokenForAccessToken(final Service service, final OAuth20AccessToken accessToken,
                                                           final AccessTokenRequestContext tokenRequestContext) throws Throwable {
        val scopes = new HashSet<>(tokenRequestContext.getScopes());
        scopes.retainAll(tokenRequestContext.getRegisteredService().getScopes());

        val credential = new BasicIdentifiableCredential(accessToken.getAuthentication().getPrincipal().getId());
        var resolvedPrincipal = principalResolver.resolve(credential);
        if (resolvedPrincipal instanceof NullPrincipal) {
            resolvedPrincipal = accessToken.getAuthentication().getPrincipal();
        }
        val exchangedPrincipal = profileScopeToAttributesFilter.filter(service, resolvedPrincipal,
            tokenRequestContext.getRegisteredService(), scopes, accessToken);

        val exchangedAuthentication = DefaultAuthenticationBuilder.newInstance(exchangedPrincipal)
            .addAttribute(OAuth20Constants.GRANT_TYPE, accessToken.getGrantType().getType());
        if (tokenRequestContext.getActorToken() != null) {
            exchangedAuthentication.addAttribute(OAuth20Constants.CLAIM_ACT,
                CollectionUtils.wrap(OAuth20Constants.CLAIM_SUB, tokenRequestContext.getActorToken().getPrincipal().getId()));
        }
        val accessTokenFactory = (OAuth20AccessTokenFactory) ticketFactory.get(OAuth20AccessToken.class);
        val ticketGrantingTicket = tokenRequestContext.getTicketGrantingTicket() == null || tokenRequestContext.getTicketGrantingTicket().isExpired()
            ? null : tokenRequestContext.getTicketGrantingTicket();
        return accessTokenFactory.create(
            service,
            exchangedAuthentication.build(),
            ticketGrantingTicket,
            scopes,
            accessToken.getId(),
            accessToken.getClientId(),
            accessToken.getClaims(),
            accessToken.getResponseType(),
            accessToken.getGrantType());
    }

    protected Ticket addAccessToken(final AccessTokenRequestContext tokenRequestContext,
                                    final OAuth20AccessToken accessToken) throws Exception {
        var finalAccessToken = (Ticket) accessToken;
        if (tokenRequestContext.getResponseType() != OAuth20ResponseTypes.ID_TOKEN && accessToken.getExpiresIn() > 0) {
            LOGGER.debug("Created access token [{}]", accessToken);
            finalAccessToken = addTicketToRegistry(accessToken, tokenRequestContext.getTicketGrantingTicket());
            LOGGER.debug("Added access token [{}] to registry", finalAccessToken);
            updateRefreshToken(tokenRequestContext, finalAccessToken);
        }
        updateOAuthCode(tokenRequestContext);
        return finalAccessToken;
    }

    protected void updateRefreshToken(final AccessTokenRequestContext tokenRequestContext,
                                    final Ticket accessToken) throws Exception {
        val trackAccessTokens = casProperties.getAuthn().getOauth().getRefreshToken().isTrackAccessTokens();
        if (tokenRequestContext.isRefreshToken() && !tokenRequestContext.getToken().isStateless() && trackAccessTokens) {
            val refreshToken = (OAuth20RefreshToken) tokenRequestContext.getToken();
            LOGGER.trace("Tracking access token [{}] linked to refresh token [{}]", accessToken.getId(), refreshToken.getId());
            refreshToken.getAccessTokens().add(accessToken.getId());
            ticketRegistry.updateTicket(refreshToken);
        }
    }

    private void updateOAuthCode(final AccessTokenRequestContext tokenRequestContext) throws Exception {
        val token = tokenRequestContext.getToken();
        if (tokenRequestContext.isCodeToken() && !token.isStateless()) {
            token.update();
            LOGGER.trace("Updated OAuth code [{}]", token.getId());
            if (token.isExpired()) {
                ticketRegistry.deleteTicket(token);
            } else {
                ticketRegistry.updateTicket(token);
            }
            updateTicketGrantingTicket(tokenRequestContext.getTicketGrantingTicket());
        }
    }

    protected Ticket addTicketToRegistry(final Ticket ticket, final Ticket ticketGrantingTicket) throws Exception {
        LOGGER.debug("Adding ticket [{}] to registry", ticket);
        val addedToken = ticketRegistry.addTicket(ticket);
        updateTicketGrantingTicket(ticketGrantingTicket);
        return addedToken;
    }

    protected Ticket addTicketToRegistry(final Ticket ticket) throws Exception {
        return addTicketToRegistry(ticket, null);
    }

    protected void updateTicketGrantingTicket(final Ticket ticketGrantingTicket) throws Exception {
        if (ticketGrantingTicket != null && !ticketGrantingTicket.isExpired()) {
            LOGGER.debug("Updating parent ticket-granting ticket [{}]", ticketGrantingTicket);
            ticketGrantingTicket.update();
            ticketRegistry.updateTicket(ticketGrantingTicket);
        }
    }
    
    protected Ticket generateRefreshToken(final AccessTokenRequestContext tokenRequestContext,
                                          final String accessTokenId) throws Throwable {
        LOGGER.debug("Creating refresh token for [{}]", tokenRequestContext.getService());
        val refreshTokenFactory = (OAuth20RefreshTokenFactory) ticketFactory.get(OAuth20RefreshToken.class);
        val ticketGrantingTicket = tokenRequestContext.getTicketGrantingTicket() == null || tokenRequestContext.getTicketGrantingTicket().isExpired()
            ? null : tokenRequestContext.getTicketGrantingTicket();
        val scopes = tokenRequestContext.getGrantType() == OAuth20GrantTypes.REFRESH_TOKEN
            ? tokenRequestContext.getToken().getScopes() : tokenRequestContext.getScopes();
        val refreshToken = refreshTokenFactory.create(tokenRequestContext.getService(),
            tokenRequestContext.getAuthentication(),
            ticketGrantingTicket,
            scopes,
            tokenRequestContext.getRegisteredService().getClientId(),
            accessTokenId,
            tokenRequestContext.getClaims(),
            tokenRequestContext.getResponseType(),
            tokenRequestContext.getGrantType());
        
        if (refreshToken.getExpirationPolicy().getTimeToLive() > 0) {
            LOGGER.debug("Adding refresh token [{}] to the registry", refreshToken);
            val addedRefreshToken = addTicketToRegistry(refreshToken, ticketGrantingTicket);
            if (tokenRequestContext.isExpireOldRefreshToken()) {
                expireOldRefreshToken(tokenRequestContext);
            }
            return addedRefreshToken;
        }
        LOGGER.debug("Refresh token expiration policy for [{}] does not allow refresh tokens to be added to the registry",
            refreshToken.getId());
        return null;
    }

    private OAuth20DeviceUserCode getDeviceUserCodeFromRegistry(final OAuth20DeviceToken deviceCodeTicket) {
        return FunctionUtils.doAndHandle(
                () -> ticketRegistry.getTicket(deviceCodeTicket.getUserCode(), OAuth20DeviceUserCode.class),
                throwable -> {
                    LOGGER.error("Provided user code [{}] is invalid or expired and cannot be found in the ticket registry",
                        deviceCodeTicket.getUserCode());
                    throw new InvalidOAuth20DeviceTokenException(deviceCodeTicket.getUserCode());
                })
            .get();
    }

    private OAuth20DeviceToken getDeviceTokenFromTicketRegistry(final String deviceCode) {
        return FunctionUtils.doAndHandle(
                () -> ticketRegistry.getTicket(deviceCode, OAuth20DeviceToken.class),
                throwable -> {
                    LoggingUtils.error(LOGGER, throwable);
                    throw new InvalidOAuth20DeviceTokenException(deviceCode);
                })
            .get();
    }

    private DeviceTokens createDeviceTokensInTicketRegistry(
        final AccessTokenRequestContext tokenRequestContext) throws Throwable {

        val deviceTokenFactory = (OAuth20DeviceTokenFactory) ticketFactory.get(OAuth20DeviceToken.class);
        val deviceUserCodeFactory = (OAuth20DeviceUserCodeFactory) ticketFactory.get(OAuth20DeviceUserCode.class);

        val deviceToken = deviceTokenFactory.createDeviceCode(tokenRequestContext.getService());
        LOGGER.debug("Created device code token [{}]", deviceToken.getId());

        val deviceUserCode = deviceUserCodeFactory.createDeviceUserCode(deviceToken.getService());
        LOGGER.debug("Created device user code token [{}]", deviceUserCode.getId());

        val addedDeviceUserCode = addTicketToRegistry(deviceUserCode);
        LOGGER.debug("Added device user code [{}] to registry", addedDeviceUserCode);

        deviceToken.setUserCode(addedDeviceUserCode.getId());
        val addedDeviceToken = addTicketToRegistry(deviceToken);
        LOGGER.debug("Added device token [{}] to registry", addedDeviceToken);

        return new DeviceTokens(addedDeviceToken, addedDeviceUserCode);
    }

    private void expireOldRefreshToken(final AccessTokenRequestContext tokenRequestContext) throws Exception {
        val oldRefreshToken = tokenRequestContext.getToken();
        if (!oldRefreshToken.isStateless()) {
            LOGGER.debug("Expiring old refresh token [{}]", oldRefreshToken);
            oldRefreshToken.markTicketExpired();
            ticketRegistry.deleteTicket(oldRefreshToken);
        }
    }

    protected record AccessAndRefreshTokens(Ticket accessToken, Ticket refreshToken) {
    }

    protected record DeviceTokens(Ticket deviceCode, Ticket userCode) {
    }
}
