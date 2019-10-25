package org.apereo.cas.support.oauth.profile;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.pac4j.core.context.JEEContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link OAuth20UserProfileDataCreator}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DefaultOAuth20UserProfileDataCreator implements OAuth20UserProfileDataCreator {

    /**
     * The services manager.
     */
    private final ServicesManager servicesManager;

    /**
     * The oauth2 scope to attributes filter.
     */
    private final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter;

    @Override
    @Audit(action = "OAUTH2_USER_PROFILE",
        actionResolverName = "OAUTH2_USER_PROFILE_ACTION_RESOLVER",
        resourceResolverName = "OAUTH2_USER_PROFILE_RESOURCE_RESOLVER")
    public Map<String, Object> createFrom(final OAuth20AccessToken accessToken, final JEEContext context) {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, accessToken.getClientId());

        val principal = getAccessTokenAuthenticationPrincipal(accessToken, context, registeredService);
        val map = new HashMap<String, Object>();
        map.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID, principal.getId());
        map.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_CLIENT_ID, accessToken.getClientId());
        val attributes = principal.getAttributes();
        map.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES, attributes);
        finalizeProfileResponse(accessToken, map, principal, registeredService);
        return map;
    }

    /**
     * Gets access token authentication principal.
     *
     * @param accessToken       the access token
     * @param context           the context
     * @param registeredService the registered service
     * @return the access token authentication principal
     */
    protected Principal getAccessTokenAuthenticationPrincipal(final OAuth20AccessToken accessToken,
                                                              final JEEContext context,
                                                              final RegisteredService registeredService) {
        val currentPrincipal = accessToken.getAuthentication().getPrincipal();
        LOGGER.debug("Preparing user profile response based on CAS principal [{}]", currentPrincipal);

        val principal = this.scopeToAttributesFilter.filter(accessToken.getService(), currentPrincipal,
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
     * @param registeredService the registered service
     */
    protected void finalizeProfileResponse(final OAuth20AccessToken accessTokenTicket,
                                           final Map<String, Object> map,
                                           final Principal principal,
                                           final RegisteredService registeredService) {
        if (registeredService instanceof OAuthRegisteredService) {
            val service = accessTokenTicket.getService();
            map.put(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        }
    }
}
