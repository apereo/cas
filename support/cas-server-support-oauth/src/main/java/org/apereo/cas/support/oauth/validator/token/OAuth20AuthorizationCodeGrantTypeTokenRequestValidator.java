package org.apereo.cas.support.oauth.validator.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.apereo.cas.ticket.OAuthToken;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20AuthorizationCodeGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20AuthorizationCodeGrantTypeTokenRequestValidator extends BaseOAuth20TokenRequestValidator {
    private final ServicesManager servicesManager;
    private final TicketRegistry ticketRegistry;
    private final OAuth20Validator validator;

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.AUTHORIZATION_CODE;
    }

    @Override
    protected boolean validateInternal(final J2EContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) {
        final HttpServletRequest request = context.getRequest();
        final String clientId = uProfile.getId();
        final String redirectUri = request.getParameter(OAuth20Constants.REDIRECT_URI);
        final OAuthRegisteredService clientRegisteredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);

        LOGGER.debug("Received grant type [{}] with client id [{}] and redirect URI [{}]", grantType, clientId, redirectUri);
        final boolean valid = this.validator.checkParameterExist(request, OAuth20Constants.REDIRECT_URI)
                && this.validator.checkParameterExist(request, OAuth20Constants.CODE)
                && this.validator.checkCallbackValid(clientRegisteredService, redirectUri);

        if (valid) {
            final String code = context.getRequestParameter(OAuth20Constants.CODE);
            final OAuthToken token = ticketRegistry.getTicket(code, OAuthCode.class);
            if (token == null || token.isExpired()) {
                LOGGER.warn("Request OAuth code [{}] is not found or has expired", code);
                return false;
            }
            final String serviceId = token.getService().getId();
            final OAuthRegisteredService codeRegisteredService =
                    OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, serviceId);
            if (!clientRegisteredService.equals(codeRegisteredService)) {
                LOGGER.warn("The OAuth code [{}] issued to service [{}] does not match the registered service [{}] provided in the request given the redirect URI [{}]",
                        code, serviceId, clientRegisteredService.getName(), redirectUri);
                return false;
            }
            return true;
        }
        LOGGER.warn("Access token request cannot be validated for grant type [{}} and client id [{}] given the redirect URI [{}]", grantType, clientId, redirectUri);
        return false;
    }
}
