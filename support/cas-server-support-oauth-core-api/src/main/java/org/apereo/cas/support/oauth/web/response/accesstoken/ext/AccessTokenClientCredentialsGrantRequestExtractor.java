package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;


/**
 * This is {@link AccessTokenClientCredentialsGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AccessTokenClientCredentialsGrantRequestExtractor extends AccessTokenPasswordGrantRequestExtractor {
    public AccessTokenClientCredentialsGrantRequestExtractor(final OAuth20ConfigurationContext oAuthConfigurationContext) {
        super(oAuthConfigurationContext);
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.CLIENT_CREDENTIALS;
    }
}
