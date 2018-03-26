package org.apereo.cas.support.oauth.validator.token;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20Validator;
import org.pac4j.core.context.J2EContext;

/**
 * This is {@link OAuth20ClientCredentialsGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20ClientCredentialsGrantTypeTokenRequestValidator extends OAuth20PasswordGrantTypeTokenRequestValidator {
    public OAuth20ClientCredentialsGrantTypeTokenRequestValidator(final ServicesManager servicesManager, final OAuth20Validator validator) {
        super(servicesManager, validator);
    }

    @Override
    public boolean supports(final J2EContext context) {
        return OAuth20Utils.isGrantType(context.getRequestParameter(OAuth20Constants.GRANT_TYPE), OAuth20GrantTypes.CLIENT_CREDENTIALS);
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.CLIENT_CREDENTIALS;
    }
}
