package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.CoreAuthenticationUtils;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.metadata.BasicCredentialMetaData;
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
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.CommonProfile;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

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

    /**
     * Build service.
     *
     * @param registeredService the registered service
     * @param context           the context
     * @param useServiceHeader  the use service header
     * @return the service
     */
    public Service buildService(final OAuthRegisteredService registeredService, final JEEContext context, final boolean useServiceHeader) {
        var id = StringUtils.EMPTY;
        if (useServiceHeader) {
            id = OAuth20Utils.getServiceRequestHeaderIfAny(context.getNativeRequest());
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
    public Authentication build(final CommonProfile profile,
                                final OAuthRegisteredService registeredService,
                                final JEEContext context,
                                final Service service) {

        val attrs = new HashMap<>(profile.getAttributes());
        attrs.putAll(profile.getAuthenticationAttributes());

        val profileAttributes = CoreAuthenticationUtils.convertAttributeValuesToMultiValuedObjects(attrs);
        val newPrincipal = this.principalFactory.createPrincipal(profile.getId(), profileAttributes);
        LOGGER.debug("Created final principal [{}] after filtering attributes based on [{}]", newPrincipal, registeredService);

        val authenticator = profile.getClass().getCanonicalName();
        val metadata = new BasicCredentialMetaData(new BasicIdentifiableCredential(profile.getId()));
        val handlerResult = new DefaultAuthenticationHandlerExecutionResult(authenticator, metadata, newPrincipal, new ArrayList<>(0));
        val scopes = CollectionUtils.toCollection(context.getNativeRequest().getParameterValues(OAuth20Constants.SCOPE));

        val state = context.getRequestParameter(OAuth20Constants.STATE).map(String::valueOf).orElse(StringUtils.EMPTY);
        val nonce = context.getRequestParameter(OAuth20Constants.NONCE).map(String::valueOf).orElse(StringUtils.EMPTY);
        LOGGER.debug("OAuth [{}] is [{}], and [{}] is [{}]", OAuth20Constants.STATE, state, OAuth20Constants.NONCE, nonce);

        /*
         * pac4j UserProfile.getPermissions() and getRoles() returns UnmodifiableSet which Jackson Serializer
         * happily serializes to json but is unable to deserialize.
         * We have to transform those to HashSet to avoid such a problem
         */
        return DefaultAuthenticationBuilder.newInstance()
            .addAttribute("permissions", new LinkedHashSet<>(profile.getPermissions()))
            .addAttribute("roles", new LinkedHashSet<>(profile.getRoles()))
            .addAttribute("scopes", scopes)
            .addAttribute(OAuth20Constants.STATE, state)
            .addAttribute(OAuth20Constants.NONCE, nonce)
            .addAttribute(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
            .addCredential(metadata)
            .setPrincipal(newPrincipal)
            .setAuthenticationDate(ZonedDateTime.now(ZoneOffset.UTC))
            .addSuccess(profile.getClass().getCanonicalName(), handlerResult)
            .build();
    }
}
