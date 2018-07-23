package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.util.CollectionUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.UserProfile;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * This is {@link OAuth20CasAuthenticationBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20CasAuthenticationBuilder {

    /**
     * The Principal factory.
     */
    protected final PrincipalFactory principalFactory;

    /**
     * The Web application service service factory.
     */
    protected final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    /**
     * Convert profile scopes to attributes.
     */
    protected final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter;

    /**
     * Collection of CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

    private static Map<String, Object> getPrincipalAttributesFromProfile(final UserProfile profile) {
        val profileAttributes = new HashMap<String, Object>(profile.getAttributes());
        profileAttributes.remove(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN);
        profileAttributes.remove(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME);
        profileAttributes.remove(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE);
        profileAttributes.remove(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS);
        profileAttributes.remove(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE);
        return profileAttributes;
    }

    private static void addAuthenticationAttribute(final String name, final AuthenticationBuilder bldr,
                                                   final UserProfile profile) {
        bldr.addAttribute(name, profile.getAttribute(name));
        LOGGER.debug("Added attribute [{}] to the authentication", name);
    }

    /**
     * Build service.
     *
     * @param registeredService the registered service
     * @param context           the context
     * @param useServiceHeader  the use service header
     * @return the service
     */
    public Service buildService(final OAuthRegisteredService registeredService, final J2EContext context, final boolean useServiceHeader) {
        var id = StringUtils.EMPTY;
        if (useServiceHeader) {
            id = OAuth20Utils.getServiceRequestHeaderIfAny(context.getRequest());
            LOGGER.debug("Located service based on request header is [{}]", id);
        }
        if (StringUtils.isBlank(id)) {
            id = registeredService.getClientId();
        }
        return webApplicationServiceServiceFactory.createService(id);
    }

    /**
     * Create an authentication from a user profile.
     *
     * @param profile           the given user profile
     * @param registeredService the registered service
     * @param context           the context
     * @param service           the service
     * @return the built authentication
     */
    public Authentication build(final UserProfile profile,
                                final OAuthRegisteredService registeredService,
                                final J2EContext context,
                                final Service service) {

        val profileAttributes = getPrincipalAttributesFromProfile(profile);
        val newPrincipal = this.principalFactory.createPrincipal(profile.getId(), profileAttributes);
        LOGGER.debug("Created final principal [{}] after filtering attributes based on [{}]", newPrincipal, registeredService);

        val authenticator = profile.getClass().getCanonicalName();
        val metadata = new BasicCredentialMetaData(new BasicIdentifiableCredential(profile.getId()));
        final AuthenticationHandlerExecutionResult handlerResult =
            new DefaultAuthenticationHandlerExecutionResult(authenticator, metadata, newPrincipal, new ArrayList<>());
        val scopes = CollectionUtils.toCollection(context.getRequest().getParameterValues(OAuth20Constants.SCOPE));

        val state = StringUtils.defaultIfBlank(context.getRequestParameter(OAuth20Constants.STATE), StringUtils.EMPTY);
        val nonce = StringUtils.defaultIfBlank(context.getRequestParameter(OAuth20Constants.NONCE), StringUtils.EMPTY);
        LOGGER.debug("OAuth [{}] is [{}], and [{}] is [{}]", OAuth20Constants.STATE, state, OAuth20Constants.NONCE, nonce);

        /*
         * pac4j UserProfile.getPermissions() and getRoles() returns UnmodifiableSet which Jackson Serializer
         * happily serializes to json but is unable to deserialize.
         * We have to transform those to HashSet to avoid such a problem
         */
        val bldr = DefaultAuthenticationBuilder.newInstance()
            .addAttribute("permissions", new LinkedHashSet<>(profile.getPermissions()))
            .addAttribute("roles", new LinkedHashSet<>(profile.getRoles()))
            .addAttribute("scopes", scopes)
            .addAttribute(OAuth20Constants.STATE, state)
            .addAttribute(OAuth20Constants.NONCE, nonce)
            .addCredential(metadata)
            .setPrincipal(newPrincipal)
            .setAuthenticationDate(ZonedDateTime.now())
            .addSuccess(profile.getClass().getCanonicalName(), handlerResult);

        collectionAuthenticationAttributesIfNecessary(profile, bldr);
        return bldr.build();
    }

    private void collectionAuthenticationAttributesIfNecessary(final UserProfile profile, final AuthenticationBuilder bldr) {
        if (casProperties.getAuthn().getOauth().getAccessToken().isReleaseProtocolAttributes()) {
            addAuthenticationAttribute(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE, bldr, profile);
            addAuthenticationAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN, bldr, profile);
            addAuthenticationAttribute(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME, bldr, profile);
            addAuthenticationAttribute(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE, bldr, profile);
            addAuthenticationAttribute(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS, bldr, profile);
        }
    }
}
