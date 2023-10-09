package org.apereo.cas.support.oauth.validator.token;

import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.jee.context.JEEContext;

/**
 * This is {@link OAuth20ClientCredentialsGrantTypeTokenRequestValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OAuth20ClientCredentialsGrantTypeTokenRequestValidator extends OAuth20PasswordGrantTypeTokenRequestValidator {
    public OAuth20ClientCredentialsGrantTypeTokenRequestValidator(final OAuth20ConfigurationContext configurationContext) {
        super(configurationContext);
    }

    @Override
    public boolean supports(final WebContext context) {
        val queryString = ((JEEContext) context).getNativeRequest().getQueryString();
        if (StringUtils.contains(queryString, OAuth20Constants.CLIENT_SECRET)) {
            LOGGER.error("Cannot accept the [{}] in the query string for [{}]", OAuth20Constants.CLIENT_SECRET, OAuth20GrantTypes.CLIENT_CREDENTIALS);
            return false;
        }

        val grantType = getConfigurationContext().getRequestParameterResolver()
            .resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return OAuth20Utils.isGrantType(grantType, OAuth20GrantTypes.CLIENT_CREDENTIALS);
    }

    @Override
    protected OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.CLIENT_CREDENTIALS;
    }
}
