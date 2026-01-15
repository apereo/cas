package org.apereo.cas.support.oauth.profile;

import module java.base;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.endpoints.OAuth20ConfigurationContext;
import org.apereo.cas.support.oauth.web.views.OAuth20UserProfileViewRenderer;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;

/**
 * Default implementation of {@link OAuth20UserProfileDataCreator}.
 *
 * @author Dmitriy Kopylenko
 * @since 5.3.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class DefaultOAuth20UserProfileDataCreator<T extends OAuth20ConfigurationContext>
    implements OAuth20UserProfileDataCreator {

    private final ObjectProvider<@NonNull T> configurationContext;

    @Override
    @Audit(action = AuditableActions.OAUTH2_USER_PROFILE,
        actionResolverName = AuditActionResolvers.OAUTH2_USER_PROFILE_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.OAUTH2_USER_PROFILE_RESOURCE_RESOLVER)
    public Map<String, Object> createFrom(final OAuth20AccessToken accessToken) throws Throwable {
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(
            configurationContext.getObject().getServicesManager(), accessToken.getClientId());
        val principal = getAccessTokenAuthenticationPrincipal(accessToken, registeredService);
        val modelAttributes = new HashMap<String, Object>();
        modelAttributes.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ID, principal.getId());
        modelAttributes.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_CLIENT_ID, accessToken.getClientId());
        modelAttributes.put(OAuth20UserProfileViewRenderer.MODEL_ATTRIBUTE_ATTRIBUTES, collectAttributes(principal, registeredService));
        finalizeProfileResponse(accessToken, modelAttributes, principal, registeredService);
        LOGGER.debug("Final user profile attributes are [{}]", modelAttributes);
        return modelAttributes;
    }

    protected Map<String, List<Object>> collectAttributes(final Principal principal,
                                                          final RegisteredService registeredService) {
        val attributes = new HashMap<>(principal.getAttributes());
        attributes.entrySet().removeIf(entry -> entry.getKey().startsWith(CentralAuthenticationService.NAMESPACE));
        return attributes;
    }

    protected Principal getAccessTokenAuthenticationPrincipal(final OAuth20AccessToken accessToken,
                                                              final RegisteredService registeredService) throws Throwable {
        val authentication = accessToken.getAuthentication();
        val attributes = new HashMap<>(authentication.getPrincipal().getAttributes());
        val authnAttributes = getConfigurationContext().getObject().getAuthenticationAttributeReleasePolicy()
            .getAuthenticationAttributesForRelease(authentication, registeredService);
        attributes.putAll(authnAttributes);
        if (accessToken.isStateless()) {
            val resolvedPrincipal = configurationContext.getObject().getPrincipalResolver()
                .resolve(new BasicIdentifiableCredential(authentication.getPrincipal().getId()));
            attributes.putAll(resolvedPrincipal.getAttributes());
        }
        val operatingPrincipal = getConfigurationContext().getObject().getPrincipalFactory()
            .createPrincipal(authentication.getPrincipal().getId(), attributes);

        LOGGER.debug("Preparing user profile response based on CAS principal [{}]", operatingPrincipal);
        val principal = configurationContext.getObject().getProfileScopeToAttributesFilter().filter(
            accessToken.getService(), operatingPrincipal, registeredService, accessToken);
        LOGGER.debug("Created CAS principal [{}] based on requested/authorized scopes", principal);
        return principal;
    }

    protected void finalizeProfileResponse(final OAuth20AccessToken accessTokenTicket,
                                           final Map<String, Object> modelAttributes,
                                           final Principal principal,
                                           final RegisteredService registeredService) {
        if (registeredService instanceof OAuthRegisteredService) {
            val service = accessTokenTicket.getService();
            modelAttributes.put(CasProtocolConstants.PARAMETER_SERVICE, service.getId());
        }
    }
}
