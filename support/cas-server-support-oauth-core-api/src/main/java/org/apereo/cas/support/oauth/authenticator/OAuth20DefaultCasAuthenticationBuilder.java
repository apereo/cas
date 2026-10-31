package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.DefaultAuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.credential.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link OAuth20DefaultCasAuthenticationBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20DefaultCasAuthenticationBuilder implements OAuth20CasAuthenticationBuilder {

    protected final PrincipalFactory principalFactory;

    protected final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    protected final OAuth20ProfileScopeToAttributesFilter scopeToAttributesFilter;

    protected final OAuth20RequestParameterResolver requestParameterResolver;

    protected final CasConfigurationProperties casProperties;

    @Override
    public Service buildService(final OAuthRegisteredService registeredService,
                                final HttpServletRequest request, final boolean useServiceHeader) {
        var serviceIdentifier = StringUtils.EMPTY;
        if (useServiceHeader) {
            serviceIdentifier = OAuth20Utils.getServiceRequestHeaderIfAny(request);
            LOGGER.debug("Located service based on request header is [{}]", serviceIdentifier);
        }
        if (StringUtils.isBlank(serviceIdentifier)) {
            serviceIdentifier = registeredService.getClientId();
        }
        val service = webApplicationServiceServiceFactory.createService(serviceIdentifier);
        service.getAttributes().put(OAuth20Constants.CLIENT_ID, List.of(registeredService.getClientId()));
        service.getAttributes().put(RegisteredService.class.getName(), List.of(registeredService.getId()));
        return service;
    }

    @Override
    public Authentication build(final OAuth20UserProfile profile,
                                final OAuthRegisteredService registeredService,
                                final HttpServletRequest request,
                                final Service service)
            throws Throwable {

        val attrs = new HashMap<>(profile.getAttributes());
        val profileAttributes = CollectionUtils.toMultiValuedMap(attrs);
        val newPrincipal = principalFactory.createPrincipal(profile.getId(), profileAttributes);
        LOGGER.debug("Created final principal [{}] after filtering attributes based on [{}]", newPrincipal, registeredService);

        val authenticator = profile.getAuthenticationHandlerName();
        val credential = new BasicIdentifiableCredential(profile.getId());
        val handlerResult = new DefaultAuthenticationHandlerExecutionResult(authenticator,
            credential, newPrincipal, new ArrayList<>());

        val scopes = requestParameterResolver.resolveRequestedScopes(request);
        scopes.retainAll(registeredService.getScopes());

        val state = Optional.ofNullable(request.getParameter(OAuth20Constants.STATE))
            .map(String::valueOf)
            .or(() -> requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.STATE))
            .orElse(StringUtils.EMPTY);
        val nonce = Optional.ofNullable(request.getParameter(OAuth20Constants.NONCE))
            .map(String::valueOf)
            .or(() -> requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.NONCE))
            .orElse(StringUtils.EMPTY);
        LOGGER.debug("OAuth [{}] is [{}], and [{}] is [{}]", OAuth20Constants.STATE, state, OAuth20Constants.NONCE, nonce);

        val builder = DefaultAuthenticationBuilder.newInstance();
        builder.addAttributes(profile.getAuthenticationAttributes());
        if (!profile.getRoles().isEmpty()) {
            builder.addAttribute("roles", new LinkedHashSet<>(profile.getRoles()));
        }
        if (!scopes.isEmpty()) {
            builder.addAttribute(OAuth20Constants.SCOPE, scopes);
        }
        FunctionUtils.doIfNotBlank(state, _ -> builder.addAttribute(OAuth20Constants.STATE, state));
        FunctionUtils.doIfNotBlank(nonce, _ -> builder.addAttribute(OAuth20Constants.NONCE, nonce));
        builder
            .addAttribute(OAuth20Constants.CLIENT_ID, registeredService.getClientId())
            .addCredential(credential)
            .setPrincipal(newPrincipal)
            .setAuthenticationDate(ZonedDateTime.now(ZoneOffset.UTC))
            .addSuccess(authenticator, handlerResult);

        Optional.ofNullable(request.getParameter(OAuth20Constants.ACR_VALUES))
            .ifPresent(value -> builder.addAttribute(OAuth20Constants.ACR_VALUES, value));

        return builder.build();
    }
}
