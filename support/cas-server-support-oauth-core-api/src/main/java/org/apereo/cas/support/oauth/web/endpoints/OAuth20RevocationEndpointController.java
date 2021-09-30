package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.ProfileManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This is {@link OAuth20RevocationEndpointController}.
 *
 * @author Julien Huon
 * @since 6.2.0
 */
@Slf4j
public class OAuth20RevocationEndpointController<T extends OAuth20ConfigurationContext> extends BaseOAuth20Controller<T> {
    public OAuth20RevocationEndpointController(final T oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    /**
     * Handle request for revocation.
     *
     * @param request  the request
     * @param response the response
     * @return the response entity
     */
    @PostMapping(path = '/' + OAuth20Constants.BASE_OAUTH20_URL + '/' + OAuth20Constants.REVOCATION_URL,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ModelAndView handleRequest(final HttpServletRequest request,
                                      final HttpServletResponse response) {
        val context = new JEEContext(request, response);

        if (!verifyRevocationRequest(context)) {
            LOGGER.error("Revocation request verification failed. Request is missing required parameters");
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
        }

        val manager = new ProfileManager(context, getConfigurationContext().getSessionStore());
        val clientId = OAuth20Utils.getClientIdAndClientSecret(context, getConfigurationContext().getSessionStore()).getLeft();
        val registeredService = getRegisteredServiceByClientId(clientId);

        if (OAuth20Utils.doesServiceNeedAuthentication(registeredService)) {
            if (manager.getProfile().isEmpty()) {
                LOGGER.warn("Service [{}] requests authentication", clientId);
                return OAuth20Utils.writeError(response, OAuth20Constants.ACCESS_DENIED);
            }
        } else {
            val service = getConfigurationContext().getWebApplicationServiceServiceFactory()
                .createService(registeredService.getServiceId());
            val audit = AuditableContext.builder()
                .service(service)
                .registeredService(registeredService)
                .build();
            val accessResult = getConfigurationContext().getRegisteredServiceAccessStrategyEnforcer().execute(audit);
            if (accessResult.isExecutionFailure()) {
                return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
            }
        }
        val token = context.getRequestParameter(OAuth20Constants.TOKEN)
            .map(String::valueOf).orElse(StringUtils.EMPTY);

        return generateRevocationResponse(token, clientId, response);
    }

    /**
     * Generate revocation token response.
     *
     * @param token the token to revoke
     * @param clientId the client who requests the revocation
     * @param response the response
     * @return the model and view
     */
    protected ModelAndView generateRevocationResponse(final String token,
                                                      final String clientId,
                                                      final HttpServletResponse response) {

        val registryToken = getConfigurationContext().getTicketRegistry().getTicket(token, OAuth20Token.class);

        if (registryToken == null) {
            LOGGER.error("Provided token [{}] has not been found in the ticket registry", token);
        } else if (isRefreshToken(registryToken) || isAccessToken(registryToken)) {
            if (!StringUtils.equals(clientId, registryToken.getClientId())) {
                LOGGER.warn("Provided token [{}] has not been issued for the service [{}]", token, clientId);
                return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
            }

            if (isRefreshToken(registryToken)) {
                revokeToken((OAuth20RefreshToken) registryToken);
            } else {
                revokeToken(registryToken.getId());
            }
        } else {
            LOGGER.error("Provided token [{}] is either not a refresh token or not an access token", token);
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
        }

        val mv = new ModelAndView(new MappingJackson2JsonView());
        mv.setStatus(HttpStatus.OK);
        return mv;
    }

    /**
     * Revoke the provided Refresh Token and it's related Access Tokens.
     *
     * @param token the token
     */
    private void revokeToken(final OAuth20RefreshToken token) {
        revokeToken(token.getId());
        token.getAccessTokens().forEach(this::revokeToken);
    }

    /**
     * Revoke the provided OAuth Token.
     *
     * @param token the token
     */
    private void revokeToken(final String token) {
        LOGGER.debug("Revoking token [{}]", token);
        getConfigurationContext().getTicketRegistry().deleteTicket(token);
    }

    /**
     * Is the OAuth token a Refresh Token?
     *
     * @param token the token
     * @return whether the token type is a RefreshToken
     */
    private static boolean isRefreshToken(final OAuth20Token token) {
        return token instanceof OAuth20RefreshToken;
    }

    /**
     * Is the OAuth token an Access Token?
     *
     * @param token the token
     * @return whether the token type is a RefreshToken
     */
    private static boolean isAccessToken(final OAuth20Token token) {
        return token instanceof OAuth20AccessToken;
    }

    /**
     * Gets registered service by client id.
     *
     * @param clientId the client id
     * @return the registered service by client id
     */
    private OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(getConfigurationContext().getServicesManager(), clientId);
    }

    /**
     * Verify the revocation request.
     *
     * @param context the context
     * @return whether the authorize request is valid
     */
    private boolean verifyRevocationRequest(final JEEContext context) {
        val validator = getConfigurationContext().getAccessTokenGrantRequestValidators().getObject()
            .stream()
            .filter(b -> b.supports(context))
            .findFirst()
            .orElse(null);
        if (validator == null) {
            LOGGER.warn("Ignoring malformed request [{}] as no OAuth20 validator could declare support for its syntax", context.getFullRequestURL());
            return false;
        }
        return validator.validate(context);
    }
}
