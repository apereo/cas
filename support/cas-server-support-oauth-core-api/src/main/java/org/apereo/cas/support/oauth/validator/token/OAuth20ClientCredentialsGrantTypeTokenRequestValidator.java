package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;

/**
 * This is {@link OAuth20ClientCredentialsGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OAuth20ClientCredentialsGrantTypeTokenRequestValidator extends OAuth20PasswordGrantTypeTokenRequestValidator {
    public OAuth20ClientCredentialsGrantTypeTokenRequestValidator(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public boolean supports(final JEEContext context) {
        val grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE).map(String::valueOf).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.CLIENT_CREDENTIALS);
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.CLIENT_CREDENTIALS;
    }
}
