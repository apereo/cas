package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.RegisteredServiceAttributeReleasePolicyContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, clientId);
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(registeredService);

            val clientSecret = clientIdAndSecret.getRight();
            if (!clientSecretValidator.validate(registeredService, clientSecret)) {
                throw new CredentialsException("Client Credentials provided is not valid for registered service: "
                                               + Objects.requireNonNull(registeredService).getName());
            }

            val redirectUri = callContext.webContext().getRequestParameter(OAuth20Constants.REDIRECT_URI)
                .map(String::valueOf).orElse(StringUtils.EMPTY);
            val service = StringUtils.isNotBlank(redirectUri)
                ? this.webApplicationServiceFactory.createService(redirectUri)
                : null;

            val authenticationResult = authenticationSystemSupport.finalizeAuthenticationTransaction(service, casCredential);
            if (authenticationResult == null) {
                throw new CredentialsException("Could not authenticate the provided credentials");
            }
            val authentication = authenticationResult.getAuthentication();
            val principal = authentication.getPrincipal();
            val context = RegisteredServiceAttributeReleasePolicyContext.builder()
                .registeredService(registeredService)
                .service(service)
                .principal(principal)
                .build();
            val attributes = Objects.requireNonNull(registeredService).getAttributeReleasePolicy().getAttributes(context);

            val profile = new CommonProfile();
            val id = registeredService.getUsernameAttributeProvider().resolveUsername(principal, service, registeredService);
            LOGGER.debug("Created profile id [{}]", id);

            profile.setId(id);
            profile.addAttributes((Map) attributes);

            val authnAttributes = authenticationAttributeReleasePolicy.getAuthenticationAttributesForRelease(authentication, registeredService);
            profile.addAuthenticationAttributes(new HashMap<>(authnAttributes));

            LOGGER.debug("Authenticated user profile [{}]", profile);
            credentials.setUserProfile(profile);
            return Optional.of(credentials);
        } catch (final Exception e) {
            throw new CredentialsException("Cannot login user using CAS internal authentication", e);
        }
    }
}
