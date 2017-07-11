package org.apereo.cas.support.oauth.validator;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.pac4j.core.context.J2EContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20AuthorizationCodeResponseTypeRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20AuthorizationCodeResponseTypeRequestValidator implements OAuth20RequestValidator {
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

        final String responseType = request.getParameter(OAuth20Constants.RESPONSE_TYPE);
        final String clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        final String redirectUri = request.getParameter(OAuth20Constants.REDIRECT_URI);
        final OAuthRegisteredService registeredService = getRegisteredServiceByClientId(clientId);

        return checkParameterExist
                && validator.checkResponseTypes(responseType, OAuth20ResponseTypes.values())
                && validator.checkServiceValid(registeredService)
                && validator.checkCallbackValid(registeredService, redirectUri);
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
