package org.apereo.cas.support.oauth.web.endpoints;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.inspektr.audit.annotation.Audit;
import org.pac4j.core.context.J2EContext;

import java.util.HashMap;
import java.util.Map;

import static org.apereo.cas.support.oauth.web.audit.Oauth2Audits.USER_PROFILE_AUDIT_ACTION;
import static org.apereo.cas.support.oauth.web.audit.Oauth2Audits.USER_PROFILE_AUDIT_ACTION_RESOLVER_NAME;
import static org.apereo.cas.support.oauth.web.audit.Oauth2Audits.USER_PROFILE_AUDIT_RESOURCE_RESOLVER_NAME;

/**
 * Default implementation of {@link OAuth2UserProfileDataCreator}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Slf4j
@AllArgsConstructor
public class DefaultOAuth2UserProfileDataCreator implements OAuth2UserProfileDataCreator {

    /**
     * The services manager.
     */
    private ServicesManager servicesManager;

    /**
     * The oauth2 scope to attributes filter.
     */
    private OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter;


    @Override
    @Audit(action = USER_PROFILE_AUDIT_ACTION,
            actionResolverName = USER_PROFILE_AUDIT_ACTION_RESOLVER_NAME,
    resourceResolverName = USER_PROFILE_AUDIT_RESOURCE_RESOLVER_NAME)
    public Map<String, Object> createFrom(final AccessToken accessToken, final J2EContext context) {
        final Principal principal = getAccessTokenAuthenticationPrincipal(accessToken, context);
        final Map<String, Object> map = new HashMap<>();

        map.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID, principal.getId());
        map.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES, principal.getAttributes());

        return map;
    }

    private Principal getAccessTokenAuthenticationPrincipal(final AccessToken accessToken, final J2EContext context) {
        final Service service = accessToken.getService();
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

        final Principal currentPrincipal = accessToken.getAuthentication().getPrincipal();
        LOGGER.debug("Preparing user profile response based on CAS principal [{}]", currentPrincipal);

        final Principal principal = this.scopeToAttributesFilter.filter(accessToken.getService(), currentPrincipal,
                registeredService, context, accessToken);
        LOGGER.debug("Created CAS principal [{}] based on requested/authorized scopes", principal);

        return principal;
    }
}
