package org.apereo.cas.support.oauth.validator;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.pac4j.core.context.J2EContext;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20RefreshTokenGrantTypeRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class OAuth20RefreshTokenGrantTypeRequestValidator implements OAuth20RequestValidator {
    private final ServicesManager servicesManager;
    private final OAuth20Validator validator;

    public OAuth20RefreshTokenGrantTypeRequestValidator(final ServicesManager servicesManager,
                                                        final OAuth20Validator validator) {
        this.servicesManager = servicesManager;
        this.validator = validator;
    }

    @Override
    public boolean validate(final J2EContext context) {
        final HttpServletRequest request = context.getRequest();
        final String clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID);
        final OAuthRegisteredService registeredService = getRegisteredServiceByClientId(clientId);
        return this.validator.checkParameterExist(request, OAuth20Constants.CLIENT_ID)
                && this.validator.checkServiceValid(registeredService)
                && this.validator.checkParameterExist(request, OAuth20Constants.SECRET)
                && this.validator.checkParameterExist(request, OAuth20Constants.REFRESH_TOKEN);

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
