package org.apereo.cas.support.oauth.validator.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.ProfileManager;
import org.pac4j.core.profile.UserProfile;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20PasswordGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20PasswordGrantTypeTokenRequestValidator extends BaseOAuth20TokenRequestValidator {
    private final ServicesManager servicesManager;
    private final OAuth20Validator validator;

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.PASSWORD;
    }

    @Override
    protected boolean validateInternal(final J2EContext context, final String grantType,
                                       final ProfileManager manager, final UserProfile uProfile) {
        final var request = context.getRequest();
        final var clientId = request.getParameter(OAuth20Constants.CLIENT_ID);
        LOGGER.debug("Received grant type [{}] with client id [{}]", grantType, clientId);
        final var registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);

        return this.validator.checkParameterExist(request, OAuth20Constants.CLIENT_ID)
            && this.validator.checkServiceValid(registeredService);
    }
}
