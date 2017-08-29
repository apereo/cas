package org.apereo.cas.support.oauth.validator;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.pac4j.core.context.J2EContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20PasswordGrantTypeRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20PasswordGrantTypeRequestValidator implements OAuth20RequestValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20PasswordGrantTypeRequestValidator.class);
    
    private final ServicesManager servicesManager;
    private final OAuth20Validator validator;

    public OAuth20PasswordGrantTypeRequestValidator(final ServicesManager servicesManager,
                                                    final OAuth20Validator validator) {
        this.servicesManager = servicesManager;
        this.validator = validator;
    }

    @Override
    public boolean validate(final J2EContext context) {
        final HttpServletRequest request = context.getRequest();

        if (!validator.checkParameterExist(request, OAuth20Constants.GRANT_TYPE)) {
            LOGGER.warn("Grant type must be specified");
            return false;
        }

        final String grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);

        if (!validator.checkParameterExist(request, OAuth20Constants.CLIENT_ID)) {
            LOGGER.warn("Client id not specified for grant type [{}]", grantType);
            return false;
        }

        if (!validator.checkParameterExist(request, OAuth20Constants.SECRET)) {
            LOGGER.warn("Client secret is not specified for grant type [{}]", grantType);
            return false;
        }

        if (!validator.checkParameterExist(request, OAuth20Constants.USERNAME)) {
            LOGGER.warn("Username is not specified for grant type [{}]", grantType);
            return false;
        }

        if (!validator.checkParameterExist(request, OAuth20Constants.PASSWORD)) {
            LOGGER.warn("Password is not specified for grant type [{}]", grantType);
            return false;
        }

        final String clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID);
        final OAuthRegisteredService registeredService = getRegisteredServiceByClientId(clientId);

        if (!validator.checkServiceValid(registeredService)) {
            LOGGER.warn("Registered service [{}] is not found or is not authorized for access.", registeredService);
            return false;
        }
        return OAuth20Utils.isAuthorizedGrantTypeForService(context, registeredService);
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
        final String grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.PASSWORD);
    }
}
