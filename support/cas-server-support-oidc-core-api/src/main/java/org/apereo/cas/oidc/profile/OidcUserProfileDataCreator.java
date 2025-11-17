package org.apereo.cas.oidc.profile;

import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfileDataCreator;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is {@link OidcUserProfileDataCreator}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class OidcUserProfileDataCreator extends DefaultOAuth20UserProfileDataCreator<OidcConfigurationContext> {
    public OidcUserProfileDataCreator(final ObjectProvider<@NonNull OidcConfigurationContext> configurationContext) {
        super(configurationContext);
    }

    @Override
    protected Map<String, List<Object>> collectAttributes(final Principal principal,
                                                          final RegisteredService registeredService) {
        val attributes = new HashMap<String, List<Object>>();
        val mapper = getConfigurationContext().getObject().getAttributeToScopeClaimMapper();
        super.collectAttributes(principal, registeredService)
            .forEach((key, value) -> {
                val collectionValues = mapper.mapClaim(key, registeredService, principal, value);
                attributes.put(key, collectionValues);
            });
        return attributes;
    }

    @Override
    protected void finalizeProfileResponse(final OAuth20AccessToken accessToken,
                                           final Map<String, Object> modelAttributes,
                                           final Principal principal,
                                           final RegisteredService registeredService) {
        super.finalizeProfileResponse(accessToken, modelAttributes, principal, registeredService);
        if (registeredService instanceof OidcRegisteredService) {
            if (accessToken.getClaims().isEmpty()) {
                if (accessToken.getTicketGrantingTicket() instanceof final AuthenticationAwareTicket aat) {
                    modelAttributes.put(OidcConstants.CLAIM_AUTH_TIME, aat.getAuthentication().getAuthenticationDate().toEpochSecond());
                }
            } else {
                modelAttributes.keySet().retainAll(CollectionUtils.wrapList(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES));
            }
        }
        if (!modelAttributes.containsKey(OAuth20Constants.CLAIM_SUB)) {
            modelAttributes.put(OAuth20Constants.CLAIM_SUB, principal.getId());
        }
        LOGGER.trace("Finalized user profile data as [{}] for access token [{}]", modelAttributes, accessToken.getId());
    }
}
