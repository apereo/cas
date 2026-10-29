package org.apereo.cas.oidc.token.ciba;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.ticket.OidcCibaRequest;
import org.apereo.cas.oidc.ticket.OidcCibaRequestFactory;
import org.apereo.cas.services.OidcBackchannelTokenDeliveryModes;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.token.OAuth20AuthorizationPendingException;
import org.apereo.cas.support.oauth.validator.token.OAuth20ExpiredTokenException;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This is {@link AccessTokenCibaGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class AccessTokenCibaGrantRequestExtractor extends BaseAccessTokenGrantRequestExtractor<OidcConfigurationContext> {
    public AccessTokenCibaGrantRequestExtractor(final ObjectProvider<OidcConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    /**
     * Extract the CIBA grant request. An expired request still held by CAS is removed and reported as
     * {@code expired_token}, and a request the end-user has not approved yet as {@code authorization_pending}
     * (CIBA Core 1.0, section 11).
     *
     * @param request  the request
     * @param response the response
     * @return the access token request context
     * @throws Throwable the throwable
     */
    @Override
    protected AccessTokenRequestContext extractRequest(final HttpServletRequest request, final HttpServletResponse response) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val profile = OAuth20Utils.findAuthenticatedUserProfile(request, response, configurationContext.getSessionStore())
            .orElseThrow(() -> UnauthorizedServiceException.denied("OAuth user profile cannot be determined"));
        val clientId = Objects.requireNonNull(profile.getAttribute(OAuth20Constants.CLIENT_ID)).toString();
        LOGGER.debug("Locating OAuth registered service by client id [{}]", clientId);

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientId, OidcRegisteredService.class);
        LOGGER.debug("Located registered service [{}]", registeredService);

        val authRequestId = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(request, OidcConstants.AUTH_REQ_ID).orElseThrow();
        val service = configurationContext.getAuthenticationBuilder()
            .buildService(registeredService, request, true);

        val cibaFactory = (OidcCibaRequestFactory) configurationContext.getTicketFactory().get(OidcCibaRequest.class);
        val decodedId = cibaFactory.decodeId(authRequestId);
        val ticket = configurationContext.getTicketRegistry().getTicket(decodedId, _ -> true);
        if (!(ticket instanceof final OidcCibaRequest cibaRequest) || !Objects.equals(cibaRequest.getClientId(), clientId)) {
            throw new InvalidCibaRequestException("CIBA request does not belong to the authenticated client");
        }
        if (cibaRequest.isExpired()) {
            LOGGER.debug("CIBA request [{}] has expired", authRequestId);
            configurationContext.getTicketRegistry().deleteTicket(cibaRequest);
            throw new OAuth20ExpiredTokenException(decodedId);
        }

        val audit = AuditableContext.builder()
            .service(service)
            .registeredService(registeredService)
            .authentication(cibaRequest.getAuthentication())
            .build();
        val accessResult = configurationContext.getRegisteredServiceAccessStrategyEnforcer().execute(audit);
        accessResult.throwExceptionIfNeeded();

        if (!registeredService.getSupportedGrantTypes().contains(getGrantType().getType())
            || StringUtils.isBlank(registeredService.getBackchannelClientNotificationEndpoint())
            || StringUtils.isBlank(registeredService.getBackchannelTokenDeliveryMode())) {
            throw new InvalidCibaRequestException("Registered OpenID Connect relying party does not support backchannel authentication requests");
        }
        val deliveryMode = OidcBackchannelTokenDeliveryModes.valueOf(registeredService.getBackchannelTokenDeliveryMode().toUpperCase(Locale.ENGLISH));
        if (deliveryMode != OidcBackchannelTokenDeliveryModes.POLL && deliveryMode != OidcBackchannelTokenDeliveryModes.PING) {
            throw new InvalidCibaRequestException("Backchannel token delivery mode cannot grant access tokens");
        }
        if (!cibaRequest.isReady()) {
            LOGGER.debug("CIBA request [{}] is not yet approved by the end-user", authRequestId);
            throw new OAuth20AuthorizationPendingException(decodedId);
        }
        
        return AccessTokenRequestContext.builder()
            .service(service)
            .authentication(cibaRequest.getAuthentication())
            .registeredService(registeredService)
            .responseType(getResponseType())
            .grantType(getGrantType())
            .scopes(cibaRequest.getScopes())
            .resources(cibaRequest.getResources())
            .userProfile(profile)
            .clientId(registeredService.getClientId())
            .cibaRequestId(authRequestId)
            .generateRefreshToken(registeredService.isGenerateRefreshToken())
            .build();

    }

    @Override
    public boolean supports(final HttpServletRequest request) {
        val configurationContext = getConfigurationContext().getObject();
        val grantType = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(request, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
        val authRequestId = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(request, OidcConstants.AUTH_REQ_ID);
        return OAuth20Utils.isGrantType(grantType, getGrantType()) && authRequestId.isPresent();
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.CIBA;
    }

    @Override
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.NONE;
    }

    @Override
    public boolean requestMustBeAuthenticated() {
        return true;
    }
}
