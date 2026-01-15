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
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.validation.AuthenticationAttributeReleasePolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Authenticator for user credentials authentication.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class OAuth20UsernamePasswordAuthenticator implements Authenticator {
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
    public Optional<Credentials> validate(final CallContext callContext, final Credentials credentials) throws CredentialsException {
        try {
            val upc = (UsernamePasswordCredentials) credentials;
            val casCredential = new UsernamePasswordCredential(upc.getUsername(), upc.getPassword());
            val clientIdAndSecret = requestParameterResolver.resolveClientIdAndClientSecret(callContext);
            if (StringUtils.isBlank(clientIdAndSecret.getKey())) {
                throw new CredentialsException("No client credentials could be identified in this request");
            }

            val clientId = clientIdAndSecret.getKey();
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, clientId);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

            val clientSecret = clientIdAndSecret.getRight();
            if (!clientSecretValidator.validate(registeredService, clientSecret)) {
                throw new CredentialsException("Client Credentials provided is not valid for registered service: "
                    + Objects.requireNonNull(registeredService).getName());
            }
            val redirectUri = requestParameterResolver.resolveRequestParameter(callContext.webContext(), OAuth20Constants.REDIRECT_URI)
                .map(String::valueOf).orElse(StringUtils.EMPTY);
            OAuth20Utils.validateRedirectUri(redirectUri);
            val service = StringUtils.isNotBlank(redirectUri)
                ? webApplicationServiceFactory.createService(redirectUri)
                : webApplicationServiceFactory.createService(clientId);
            service.getAttributes().put(OAuth20Constants.CLIENT_ID, CollectionUtils.wrapList(clientId));
            service.getAttributes().put(OAuth20Constants.REDIRECT_URI, CollectionUtils.wrapList(redirectUri));

            val authenticationResult = authenticationSystemSupport.finalizeAuthenticationTransaction(service, casCredential);
            if (authenticationResult == null) {
                throw new CredentialsException("Could not authenticate the provided credentials");
            }

            val principal = buildAuthenticatedPrincipal(authenticationResult, registeredService, service, callContext);
            val profile = new CommonProfile();

            profile.setId(principal.getId());
            profile.addAttribute(OAuth20Constants.CLIENT_ID, clientId);
            profile.addAttributes((Map) principal.getAttributes());

            val authentication = authenticationResult.getAuthentication();
            val authnAttributes = authenticationAttributeReleasePolicy.getAuthenticationAttributesForRelease(authentication, registeredService);
            profile.addAuthenticationAttributes(new HashMap<>(authnAttributes));

            LOGGER.debug("Authenticated user profile [{}]", profile);
            credentials.setUserProfile(profile);
            return Optional.of(credentials);
        } catch (final Throwable e) {
            throw new CredentialsException("Cannot login user using CAS internal authentication", e);
        }
    }

    protected Principal buildAuthenticatedPrincipal(final AuthenticationResult authenticationResult,
                                                    final OAuthRegisteredService registeredService,
                                                    final Service service, final CallContext callContext) throws Throwable {
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

        val accessTokenFactory = (OAuth20AccessTokenFactory) ticketFactory.get(OAuth20AccessToken.class);
        val scopes = requestParameterResolver.resolveRequestedScopes(callContext.webContext());
        val responseType = requestParameterResolver.resolveResponseType(callContext.webContext());
        val grantType = requestParameterResolver.resolveGrantType(callContext.webContext());
        val accessToken = accessTokenFactory.create(service, authentication, scopes,
            registeredService.getClientId(), responseType, grantType);
        val finalPrincipal = profileScopeToAttributesFilter.filter(service, principal, registeredService, accessToken);
        LOGGER.debug("Built final principal [{}]", finalPrincipal);
        return finalPrincipal;
    }
}
