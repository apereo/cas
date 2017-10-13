package org.apereo.cas.support.oauth.authenticator;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.BasicCredentialMetaData;
import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.CredentialMetaData;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultHandlerResult;
import org.apereo.cas.authentication.HandlerResult;
import org.apereo.cas.authentication.principal.Principal;
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
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.profile.UserProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This is {@link OAuth20CasAuthenticationBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class OAuth20CasAuthenticationBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(OAuth20CasAuthenticationBuilder.class);

    /**
     * Collection of CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

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

    public OAuth20CasAuthenticationBuilder(final PrincipalFactory principalFactory,
                                           final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                           final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter,
                                           final CasConfigurationProperties casProperties) {
        this.principalFactory = principalFactory;
        this.webApplicationServiceServiceFactory = webApplicationServiceServiceFactory;
        this.scopeToAttributesFilter = scopeToAttributesFilter;
        this.casProperties = casProperties;
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
        String id = null;
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

        final Map<String, Object> profileAttributes = getPrincipalAttributesFromProfile(profile);
        final Principal newPrincipal = this.principalFactory.createPrincipal(profile.getId(), profileAttributes);
        LOGGER.debug("Created final principal [{}] after filtering attributes based on [{}]", newPrincipal, registeredService);

        final String authenticator = profile.getClass().getCanonicalName();
        final CredentialMetaData metadata = new BasicCredentialMetaData(new BasicIdentifiableCredential(profile.getId()));
        final HandlerResult handlerResult = new DefaultHandlerResult(authenticator, metadata, newPrincipal, new ArrayList<>());
        final Set<Object> scopes = CollectionUtils.toCollection(context.getRequest().getParameterValues(OAuth20Constants.SCOPE));

        final String state = StringUtils.defaultIfBlank(context.getRequestParameter(OAuth20Constants.STATE), StringUtils.EMPTY);
        final String nonce = StringUtils.defaultIfBlank(context.getRequestParameter(OAuth20Constants.NONCE), StringUtils.EMPTY);
        LOGGER.debug("OAuth [{}] is [{}], and [{}] is [{}]", OAuth20Constants.STATE, state, OAuth20Constants.NONCE, nonce);

        /*
         * pac4j UserProfile.getPermissions() and getRoles() returns UnmodifiableSet which Jackson Serializer
         * happily serializes to json but is unable to deserialize.
         * We have to wrap it to HashSet to avoid such problem
         */
        final AuthenticationBuilder bldr = DefaultAuthenticationBuilder.newInstance()
                .addAttribute("permissions", new HashSet<>(profile.getPermissions()))
                .addAttribute("roles", new HashSet<>(profile.getRoles()))
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

    private static Map<String, Object> getPrincipalAttributesFromProfile(final UserProfile profile) {
        final Map<String, Object> profileAttributes = new HashMap<>(profile.getAttributes());
        profileAttributes.remove(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_FROM_NEW_LOGIN);
        profileAttributes.remove(CasProtocolConstants.VALIDATION_REMEMBER_ME_ATTRIBUTE_NAME);
        profileAttributes.remove(AuthenticationManager.AUTHENTICATION_METHOD_ATTRIBUTE);
        profileAttributes.remove(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS);
        profileAttributes.remove(CasProtocolConstants.VALIDATION_CAS_MODEL_ATTRIBUTE_NAME_AUTHENTICATION_DATE);
        return profileAttributes;
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

    private static void addAuthenticationAttribute(final String name, final AuthenticationBuilder bldr,
                                                   final UserProfile profile) {
        bldr.addAttribute(name, profile.getAttribute(name));
        LOGGER.debug("Added attribute [{}] to the authentication", name);
    }
}
