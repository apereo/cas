package org.apereo.cas.support.oauth.validator;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.pac4j.core.context.J2EContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20AuthorizationCodeResponseTypeRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20AuthorizationCodeResponseTypeRequestValidator implements OAuth20RequestValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20AuthorizationCodeResponseTypeRequestValidator.class);

    private final ServicesManager servicesManager;
    private final OAuth20Validator validator;

    public OAuth20AuthorizationCodeResponseTypeRequestValidator(final ServicesManager servicesManager,
                                                                final OAuth20Validator validator) {
        this.servicesManager = servicesManager;
        this.validator = validator;
    }

    @Override
    public boolean validate(final J2EContext context) {
        final HttpServletRequest request = context.getRequest();
        final boolean checkParameterExist = validator.checkParameterExist(request, OAuth20Constants.CLIENT_ID)
                && validator.checkParameterExist(request, OAuth20Constants.REDIRECT_URI)
                && validator.checkParameterExist(request, OAuth20Constants.RESPONSE_TYPE);

        if (!checkParameterExist) {
            LOGGER.warn("Missing required parameters (client id, redirect uri, etc) for response type [{}].", getResponseType());
            return false;
        }

        final String responseType = request.getParameter(OAuth20Constants.RESPONSE_TYPE);
        if (!validator.checkResponseTypes(responseType, OAuth20ResponseTypes.values())) {
            LOGGER.warn("Response type [{}] is not supported.", responseType);
            return false;
        }

        final String clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        final OAuthRegisteredService registeredService = getRegisteredServiceByClientId(clientId);

        if (!validator.checkServiceValid(registeredService)) {
            LOGGER.warn("Registered service [{}] is not found or is not authorized for access.", registeredService);
            return false;
        }

        final String redirectUri = request.getParameter(OAuth20Constants.REDIRECT_URI);
        if (!validator.checkCallbackValid(registeredService, redirectUri)) {
            LOGGER.warn("Callback URL [{}] is not authorized for registered service [{}].", redirectUri, registeredService);
            return false;
        }
        
        return OAuth20Utils.isAuthorizedResponseTypeForService(context, registeredService);
    }

    /**
     * Gets registered service by client id.
     *
     * @param clientId the client id
     * @return the registered service by client id
     */
    protected OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        return OAuth20Utils.getRegisteredOAuthService(this.servicesManager, clientId);
    }

    @Override
    public boolean supports(final J2EContext context) {
        final String grantType = context.getRequestParameter(OAuth20Constants.RESPONSE_TYPE);
        return OAuth20Utils.isResponseType(grantType, getResponseType());
    }

    /**
     * Gets response type.
     *
     * @return the response type
     */
    public OAuth20ResponseTypes getResponseType() {
        return OAuth20ResponseTypes.CODE;
    }
}
