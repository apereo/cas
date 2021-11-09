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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.profile.BasicUserProfile;
import org.pac4j.core.profile.UserProfile;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * This is {@link OAuth20DefaultCasAuthenticationBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20DefaultCasAuthenticationBuilder implements OAuth20CasAuthenticationBuilder {

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

    @Override
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

    @Override
    public Authentication build(final UserProfile profile,
                                final OAuthRegisteredService registeredService,
                                final JEEContext context,
                                final Service service) {

        val attrs = new HashMap<>(profile.getAttributes());

        val profileAttributes = CoreAuthenticationUtils.convertAttributeValuesToMultiValuedObjects(attrs);
        val newPrincipal = principalFactory.createPrincipal(profile.getId(), profileAttributes);
        LOGGER.debug("Created final principal [{}] after filtering attributes based on [{}]", newPrincipal, registeredService);

        val authenticator = profile.getClass().getCanonicalName();
        val metadata = new BasicCredentialMetaData(new BasicIdentifiableCredential(profile.getId()));
        val handlerResult = new DefaultAuthenticationHandlerExecutionResult(authenticator, metadata, newPrincipal, new ArrayList<>(0));

        val scopes = OAuth20Utils.getRequestedScopes(context);
        val state = context.getRequestParameter(OAuth20Constants.STATE)
            .map(String::valueOf)
            .or(() -> OAuth20Utils.getRequestParameter(context, OAuth20Constants.STATE))
            .orElse(StringUtils.EMPTY);
        val nonce = context.getRequestParameter(OAuth20Constants.NONCE)
            .map(String::valueOf)
            .or(() -> OAuth20Utils.getRequestParameter(context, OAuth20Constants.NONCE))
            .orElse(StringUtils.EMPTY);
        LOGGER.debug("OAuth [{}] is [{}], and [{}] is [{}]", OAuth20Constants.STATE, state, OAuth20Constants.NONCE, nonce);

        val builder = DefaultAuthenticationBuilder.newInstance();
        if (profile instanceof BasicUserProfile) {
            val authenticationAttributes = ((BasicUserProfile) profile).getAuthenticationAttributes();
            builder.addAttributes(authenticationAttributes);
        }

        builder
            .addAttribute("permissions", new LinkedHashSet<>(profile.getPermissions()))
            .addAttribute("roles", new LinkedHashSet<>(profile.getRoles()))
            .addAttribute("scopes", scopes)
            .addAttribute(OAuth20Constants.STATE, state)
            .addAttribute(OAuth20Constants.NONCE, nonce)
            .addAttribute(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
            .addCredential(metadata)
            .setPrincipal(newPrincipal)
            .setAuthenticationDate(ZonedDateTime.now(ZoneOffset.UTC))
            .addSuccess(profile.getClass().getCanonicalName(), handlerResult);

        context.getRequestParameter(OAuth20Constants.ACR_VALUES)
            .ifPresent(value -> builder.addAttribute(OAuth20Constants.ACR_VALUES, value));

        return builder.build();
    }
}
