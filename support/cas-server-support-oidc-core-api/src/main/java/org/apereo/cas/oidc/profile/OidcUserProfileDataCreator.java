package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * This is {@link OidcUserProfileDataCreator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OidcUserProfileDataCreator extends DefaultOAuth20UserProfileDataCreator {
    public OidcUserProfileDataCreator(final ServicesManager servicesManager,
                                      final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter) {
        super(servicesManager, scopeToAttributesFilter);
    }

    @Override
    protected void finalizeProfileResponse(final OAuth20AccessToken accessToken,
                                           final Map<String, Object> map,
                                           final Principal principal,
                                           final RegisteredService registeredService) {
        super.finalizeProfileResponse(accessToken, map, principal, registeredService);

        if (registeredService instanceof OidcRegisteredService) {
            if (accessToken.getClaims().isEmpty()) {
                if (!map.containsKey(OidcConstants.CLAIM_SUB)) {
                    map.put(OidcConstants.CLAIM_SUB, principal.getId());
                }
                map.put(OidcConstants.CLAIM_AUTH_TIME, accessToken.getAuthentication().getAuthenticationDate().toEpochSecond());
            } else {
                map.keySet().retainAll(CollectionUtils.wrapList(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES));
            }
        }
        LOGGER.trace("Finalized user profile data as [{}] for access token [{}]", map, accessToken.getId());
    }
}
