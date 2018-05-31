package org.apereo.cas.support.oauth.profile;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.accesstoken.AccessToken;
import org.apereo.inspektr.audit.annotation.Audit;
import org.pac4j.core.context.J2EContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link OAuth20UserProfileDataCreator}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Slf4j
@AllArgsConstructor
public class DefaultOAuth20UserProfileDataCreator implements OAuth20UserProfileDataCreator {

    /**
     * The services manager.
     */
    private ServicesManager servicesManager;

    /**
     * The oauth2 scope to attributes filter.
     */
    private OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter;

    @Override
    @Audit(action = "OAUTH2_USER_PROFILE_DATA",
        actionResolverName = "OAUTH2_USER_PROFILE_DATA_ACTION_RESOLVER",
        resourceResolverName = "OAUTH2_USER_PROFILE_DATA_RESOURCE_RESOLVER")
    public Map<String, Object> createFrom(final AccessToken accessToken, final J2EContext context) {
        final var principal = getAccessTokenAuthenticationPrincipal(accessToken, context);
        final Map<String, Object> map = new HashMap<>();
        map.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID, principal.getId());
        map.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES, principal.getAttributes());
        finalizeProfileResponse(accessToken, map, principal);
        return map;
    }

    /**
     * Gets access token authentication principal.
     *
     * @param accessToken the access token
     * @param context     the context
     * @return the access token authentication principal
     */
    protected Principal getAccessTokenAuthenticationPrincipal(final AccessToken accessToken, final J2EContext context) {
        final var service = accessToken.getService();
        final var registeredService = this.servicesManager.findServiceBy(service);

        final var currentPrincipal = accessToken.getAuthentication().getPrincipal();
        LOGGER.debug("Preparing user profile response based on CAS principal [{}]", currentPrincipal);

        final var principal = this.scopeToAttributesFilter.filter(accessToken.getService(), currentPrincipal,
            registeredService, context, accessToken);
        LOGGER.debug("Created CAS principal [{}] based on requested/authorized scopes", principal);

        return principal;
    }

    /**
     * Finalize profile response.
     *
     * @param accessTokenTicket the access token ticket
     * @param map               the map
     * @param principal         the authentication principal
     */
    protected void finalizeProfileResponse(final AccessToken accessTokenTicket, final Map<String, Object> map, final Principal principal) {
        final var service = accessTokenTicket.getService();
        final var registeredService = servicesManager.findServiceBy(service);
        if (registeredService instanceof OAuthRegisteredService) {
            final var oauth = (OAuthRegisteredService) registeredService;
            map.put(OAuth20Constants.CLIENT_ID, oauth.getClientId());
            map.put(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        }
    }
}
