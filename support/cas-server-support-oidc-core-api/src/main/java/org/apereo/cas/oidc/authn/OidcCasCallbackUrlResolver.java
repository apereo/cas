package org.apereo.cas.oidc.authn;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20CasCallbackUrlResolver;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;

import lombok.val;

import java.util.List;

/**
 * This is {@link OidcCasCallbackUrlResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public class OidcCasCallbackUrlResolver extends OAuth20CasCallbackUrlResolver {
    public OidcCasCallbackUrlResolver(final CasConfigurationProperties casProperties,
                                      final OAuth20RequestParameterResolver oauthRequestParameterResolver) {
        super(OAuth20Utils.casOAuthCallbackUrl(casProperties.getServer().getPrefix()), oauthRequestParameterResolver);
    }

    @Override
    protected List<String> getIncludeParameterNames() {
        val list = super.getIncludeParameterNames();
        list.add(OidcConstants.UI_LOCALES);
        list.add(OidcConstants.MAX_AGE);
        list.add(OidcConstants.LOGIN_HINT);
        list.add(OidcConstants.REQUEST_URI);
        return list;
    }
}
