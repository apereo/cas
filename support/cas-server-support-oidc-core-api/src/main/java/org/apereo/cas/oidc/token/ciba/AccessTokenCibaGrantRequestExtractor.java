package org.apereo.cas.oidc.token.ciba;

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
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.AccessTokenRequestContext;
import org.apereo.cas.support.oauth.web.response.accesstoken.ext.BaseAccessTokenGrantRequestExtractor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.beans.factory.ObjectProvider;
import java.util.Locale;

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

    @Override
    protected AccessTokenRequestContext extractRequest(final WebContext context) throws Throwable {
        val configurationContext = getConfigurationContext().getObject();
        val manager = new ProfileManager(context, configurationContext.getSessionStore());
        val profile = manager.getProfile().orElseThrow(() -> UnauthorizedServiceException.denied("OAuth user profile cannot be determined"));
        val clientId = profile.getAttribute(OAuth20Constants.CLIENT_ID).toString();
        LOGGER.debug("Locating OAuth registered service by client id [{}]", clientId);

        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(configurationContext.getServicesManager(), clientId, OidcRegisteredService.class);
        LOGGER.debug("Located registered service [{}]", registeredService);

        val authRequestId = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(context, OidcConstants.AUTH_REQ_ID).orElseThrow();
        val service = configurationContext.getAuthenticationBuilder()
            .buildService(registeredService, context, true);

        val cibaFactory = (OidcCibaRequestFactory) configurationContext.getTicketFactory().get(OidcCibaRequest.class);
        val decodedId = cibaFactory.decodeId(authRequestId);
        val cibaRequest = configurationContext.getTicketRegistry().getTicket(decodedId, OidcCibaRequest.class);

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
            throw new InvalidCibaRequestException("CIBA request %s is not ready to grant access tokens".formatted(authRequestId));
        }
        
        return AccessTokenRequestContext.builder()
            .service(service)
            .authentication(cibaRequest.getAuthentication())
            .registeredService(registeredService)
            .responseType(getResponseType())
            .grantType(getGrantType())
            .scopes(cibaRequest.getScopes())
            .userProfile(profile)
            .clientId(registeredService.getClientId())
            .generateRefreshToken(registeredService.isGenerateRefreshToken())
            .build();

    }

    @Override
    public boolean supports(final WebContext context) {
        val configurationContext = getConfigurationContext().getObject();
        val grantType = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE).orElse(StringUtils.EMPTY);
        val authRequestId = configurationContext.getRequestParameterResolver()
            .resolveRequestParameter(context, OidcConstants.AUTH_REQ_ID);
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
