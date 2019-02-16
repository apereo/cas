package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.ticket.accesstoken.AccessToken;

import java.util.Map;

/**
 * This is {@link OidcUserProfileDataCreator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class OidcUserProfileDataCreator extends DefaultOAuth20UserProfileDataCreator {
    public OidcUserProfileDataCreator(final ServicesManager servicesManager,
                                      final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter) {
        super(servicesManager, scopeToAttributesFilter);
    }

    @Override
    protected void finalizeProfileResponse(final AccessToken accessToken, final Map<String, Object> map, final Principal principal) {
        if (!map.containsKey(OidcConstants.CLAIM_SUB)) {
            map.put(OidcConstants.CLAIM_SUB, principal.getId());
        }
        map.put(OidcConstants.CLAIM_AUTH_TIME, accessToken.getAuthentication().getAuthenticationDate().toEpochSecond());
    }
}
