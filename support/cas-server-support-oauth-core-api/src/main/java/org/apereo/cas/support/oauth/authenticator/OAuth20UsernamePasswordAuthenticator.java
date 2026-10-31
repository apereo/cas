package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceUsernameProviderContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.authentication.BadCredentialsException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Authenticator for user credentials authentication.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class OAuth20UsernamePasswordAuthenticator implements OAuth20ClientCredentialsAuthenticator {
    private final AuthenticationSystemSupport authenticationSystemSupport;

    private final ServicesManager servicesManager;

    private final ServiceFactory webApplicationServiceFactory;

    private final OAuth20RequestParameterResolver requestParameterResolver;

    private final OAuth20ClientSecretValidator clientSecretValidator;

    private final AuthenticationAttributeReleasePolicy authenticationAttributeReleasePolicy;

    private final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    private final TicketFactory ticketFactory;

    private final ConfigurableApplicationContext applicationContext;

    @Override
    public Optional<OAuth20UserProfile> authenticate(final OAuth20ClientAuthenticationToken token) throws BadCredentialsException {
        try {
            val request = token.getRequest();
            val casCredential = new UsernamePasswordCredential(Objects.requireNonNull(token.getUsername()),
                Objects.requireNonNull((String) token.getCredentials()));
            val clientIdAndSecret = requestParameterResolver.resolveClientIdAndClientSecret(request);
            if (StringUtils.isBlank(clientIdAndSecret.getKey())) {
                throw new BadCredentialsException("No client credentials could be identified in this request");
            }

            val clientId = clientIdAndSecret.getKey();
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

            val clientSecret = clientIdAndSecret.getRight();
            if (!clientSecretValidator.validate(registeredService, clientSecret)) {
                throw new BadCredentialsException("Client Credentials provided is not valid for registered service: "
                    + Objects.requireNonNull(registeredService).getName());
            }
            val redirectUri = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.REDIRECT_URI)
                .map(String::valueOf).orElse(StringUtils.EMPTY);
            OAuth20Utils.validateRedirectUri(redirectUri, true);
            val service = StringUtils.isNotBlank(redirectUri)
                ? webApplicationServiceFactory.createService(redirectUri)
                : webApplicationServiceFactory.createService(clientId);
            service.getAttributes().put(OAuth20Constants.CLIENT_ID, CollectionUtils.wrapList(clientId));
            service.getAttributes().put(OAuth20Constants.REDIRECT_URI, CollectionUtils.wrapList(redirectUri));

            val authenticationResult = authenticationSystemSupport.finalizeAuthenticationTransaction(service, casCredential);
            if (authenticationResult == null) {
                throw new BadCredentialsException("Could not authenticate the provided credentials");
            }

            val principal = buildAuthenticatedPrincipal(authenticationResult, registeredService, service, request);
            val profile = new DefaultOAuth20UserProfile(principal.getId(), OAuth20UserProfile.COMMON_PROFILE_HANDLER_NAME);
            profile.addAttribute(OAuth20Constants.CLIENT_ID, clientId);
            profile.addAttributes(principal.getAttributes());

            val authentication = authenticationResult.getAuthentication();
            val authnAttributes = authenticationAttributeReleasePolicy.getAuthenticationAttributesForRelease(authentication, registeredService);
            profile.addAuthenticationAttributes(new HashMap<>(authnAttributes));

            LOGGER.debug("Authenticated user profile [{}]", profile);
            return Optional.of(profile);
        } catch (final Throwable e) {
            throw new BadCredentialsException("Cannot login user using CAS internal authentication", e);
        }
    }

    protected Principal buildAuthenticatedPrincipal(final AuthenticationResult authenticationResult,
                                                    final OAuthRegisteredService registeredService,
                                                    final Service service, final HttpServletRequest request)
            throws Throwable {
        val authentication = authenticationResult.getAuthentication();
        val principal = authentication.getPrincipal();

        val usernameContext = RegisteredServiceUsernameProviderContext
            .builder()
            .registeredService(registeredService)
            .service(service)
            .principal(principal)
            .applicationContext(applicationContext)
            .build();
        val id = registeredService.getUsernameAttributeProvider().resolveUsername(usernameContext);
        LOGGER.debug("Created profile id [{}]", id);

        val scopes = new LinkedHashSet<>(resolveRequestedScopes(request));
        val finalPrincipal = profileScopeToAttributesFilter.filter(service, principal, registeredService, scopes, null);
        LOGGER.debug("Built final principal [{}]", finalPrincipal);
        return finalPrincipal;
    }

    protected Collection<String> resolveRequestedScopes(final HttpServletRequest request) {
        return requestParameterResolver.resolveRequestedScopes(request);
    }
}
