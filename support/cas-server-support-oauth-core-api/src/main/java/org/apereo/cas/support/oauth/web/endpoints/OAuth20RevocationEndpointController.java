package org.apereo.cas.support.oauth.web.endpoints;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.CommonProfile;
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
public class OAuth20RevocationEndpointController extends BaseOAuth20Controller {
    public OAuth20RevocationEndpointController(final OAuth20ConfigurationContext oAuthConfigurationContext) {
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
        val context = new JEEContext(request, response, getOAuthConfigurationContext().getSessionStore());

        if (!verifyRevocationRequest(context)) {
            LOGGER.error("Revocation request verification failed. Request is missing required parameters");
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
        }

        val manager = new ProfileManager<CommonProfile>(context, context.getSessionStore());
        val clientId = OAuth20Utils.getClientIdAndClientSecret(context).getLeft();
        val registeredService = getRegisteredServiceByClientId(clientId);

        if (isServiceNeedAuthentication(registeredService)) {
            if (manager.get(true).isEmpty()) {
                LOGGER.error("Service [{}] requests authentication", clientId);
                return OAuth20Utils.writeError(response, OAuth20Constants.ACCESS_DENIED);
            }
        }
        val token = context.getRequestParameter(OAuth20Constants.TOKEN)
            .map(String::valueOf).orElse(StringUtils.EMPTY);

        if (isAsupportedTokenType(token)) {
            return generateRevocationResponse(token, clientId, response);
        }
        return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
    }

    /**
     * Generate revocation token response.
     *
     * @param token the token to revoke
     * @param client the client who requests the revocation
     * @param response the response
     * @return the model and view
     */
    private ModelAndView generateRevocationResponse(final String token,
                                                    final String client,
                                                    final HttpServletResponse response) {

        if (token.startsWith(OAuth20RefreshToken.PREFIX)) {
            val registryToken = getOAuthConfigurationContext().getTicketRegistry().getTicket(token, OAuth20RefreshToken.class);

            if (registryToken == null) {
                LOGGER.error("Provided token [{}] has not been found in the ticket registry", token);
            } else {
                if (!StringUtils.equals(client, registryToken.getClientId())) {
                    LOGGER.error("Provided token [{}] is not related with the service [{}]", token, client);
                    return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
                }
                revokeToken(registryToken);
            }
        } else {
            val registryToken = getOAuthConfigurationContext().getTicketRegistry().getTicket(token, OAuth20AccessToken.class);

            if (registryToken == null) {
                LOGGER.error("Provided token [{}] has not been found in the ticket registry", token);
            } else {
                if (!StringUtils.equals(client, registryToken.getClientId())) {
                    LOGGER.error("Provided token [{}] is not related with the service [{}]", token, client);
                    return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
                }
                revokeToken(registryToken);
            }
        }

        val mv = new ModelAndView(new MappingJackson2JsonView());
        mv.setStatus(HttpStatus.OK);
        return mv;
    }

    /**
     * Revoke the provided Refresh Token and it's related Access Tokens.
     *
     * @param token the token
     * @return the model and view
     */
    private void revokeToken(final OAuth20RefreshToken token) {
        LOGGER.debug("Revoking token [{}]", token);
        getOAuthConfigurationContext().getTicketRegistry().deleteTicket(token);

        token.getAccessTokens().forEach(item-> {
            LOGGER.debug("Revoking Access Token [{}] related to Refresh Token [{}]", item, token);
            getOAuthConfigurationContext().getTicketRegistry().deleteTicket(item);
        });
    }

    /**
     * Revoke the provided Access Token.
     *
     * @param token the token
     * @return the model and view
     */
    private void revokeToken(final OAuth20AccessToken token) {
        LOGGER.debug("Revoking token [{}]", token);
        getOAuthConfigurationContext().getTicketRegistry().deleteTicket(token);
    }

    /**
     * Verify if the request related token type is supported.
     *
     * @param token the token
     * @return whether the token type is supported
     */
    private boolean isAsupportedTokenType(final String token) {
        return token.startsWith(OAuth20RefreshToken.PREFIX) || token.startsWith(OAuth20AccessToken.PREFIX);
    }

    /**
     * Gets registered service by client id.
     *
     * @param clientId the client id
     * @return the registered service by client id
     */
    private OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        return OAuth20Utils.getRegisteredOAuthServiceByClientId(getOAuthConfigurationContext().getServicesManager(), clientId);
    }

    /**
     * Verify if the request related service must be authenticated.
     *
     * @param registeredService the registered service
     * @return whether the service need authentication
     */
    private boolean isServiceNeedAuthentication(final OAuthRegisteredService registeredService) {
        return !StringUtils.isBlank(registeredService.getClientSecret());
    }

    /**
     * Verify the revocation request.
     *
     * @param context the context
     * @return whether the authorize request is valid
     */
    private boolean verifyRevocationRequest(final JEEContext context) {
        val validator = getOAuthConfigurationContext().getAccessTokenGrantRequestValidators()
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
