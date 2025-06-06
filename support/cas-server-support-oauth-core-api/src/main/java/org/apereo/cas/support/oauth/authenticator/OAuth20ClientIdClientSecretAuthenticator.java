package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.DefaultAuthenticationBuilder;
import org.apereo.cas.authentication.principal.NullPrincipal;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredServiceUsernameProviderContext;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessTokenFactory;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.CredentialSource;
import org.pac4j.core.credentials.Credentials;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Map;
import java.util.Optional;

/**
 * Authenticator for client credentials authentication.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class OAuth20ClientIdClientSecretAuthenticator implements Authenticator {
    private final ServicesManager servicesManager;

    private final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final TicketRegistry ticketRegistry;

    private final PrincipalResolver principalResolver;

    private final OAuth20RequestParameterResolver requestParameterResolver;

    private final OAuth20ClientSecretValidator clientSecretValidator;

    private final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter;

    private final TicketFactory ticketFactory;

    private final ConfigurableApplicationContext applicationContext;

    @Override
    public Optional<Credentials> validate(final CallContext callContext, final Credentials credentials) {
        return FunctionUtils.doUnchecked(() -> {
            LOGGER.debug("Authenticating credential [{}]", credentials);
            val upc = (UsernamePasswordCredentials) credentials;
            val id = upc.getUsername();
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, id);
            val audit = AuditableContext.builder()
                .registeredService(registeredService)
                .build();
            val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);
            val proceed = !accessResult.isExecutionFailure() && canAuthenticate(callContext);
            if (!proceed) {
                val name = getClass().getSimpleName();
                LOGGER.debug("Skipping authenticator [{}]; service access is rejected for [{}] or the authentication request is not supported", name, registeredService);
                return Optional.empty();
            }

            val requiredAuthnMethod = CredentialSource.FORM.name().equalsIgnoreCase(upc.getSource())
                ? OAuth20ClientAuthenticationMethods.CLIENT_SECRET_POST
                : OAuth20ClientAuthenticationMethods.CLIENT_SECRET_BASIC;
            if (!isAuthenticationMethodSupported(callContext, registeredService, requiredAuthnMethod)) {
                LOGGER.warn("Client authentication method [{}] is not supported for service [{}]", requiredAuthnMethod, registeredService.getName());
                return Optional.empty();
            }

            validateCredentials(upc, registeredService, callContext);

            val credential = new OAuth20ClientIdClientSecretCredential(upc.getUsername(), upc.getPassword());
            val resolvedPrincipal = principalResolver.resolve(credential);

            val service = webApplicationServiceServiceFactory.createService(registeredService.getServiceId());
            val profile = new CommonProfile();
            if (resolvedPrincipal instanceof NullPrincipal) {
                LOGGER.debug("No principal was resolved. Falling back to the username [{}] from the credentials.", id);
                profile.setId(id);
            } else {
                val usernameContext = RegisteredServiceUsernameProviderContext.builder()
                    .registeredService(registeredService)
                    .service(service)
                    .principal(resolvedPrincipal)
                    .applicationContext(applicationContext)
                    .build();
                val username = registeredService.getUsernameAttributeProvider().resolveUsername(usernameContext);
                profile.setId(username);
            }
            profile.addAttribute(OAuth20Constants.CLIENT_ID, id);
            LOGGER.debug("Created profile id [{}]", profile.getId());

            val principal = buildAuthenticatedPrincipal(resolvedPrincipal, registeredService, service, callContext);
            profile.addAttributes((Map) principal.getAttributes());

            LOGGER.debug("Authenticated user profile [{}]", profile);
            credentials.setUserProfile(profile);
            return Optional.of(credentials);
        });
    }

    protected boolean isAuthenticationMethodSupported(final CallContext callContext, final OAuthRegisteredService registeredService,
                                                      final OAuth20ClientAuthenticationMethods requiredAuthnMethod) {
        return OAuth20Utils.isTokenAuthenticationMethodSupportedFor(callContext, registeredService, requiredAuthnMethod);
    }

    protected Principal buildAuthenticatedPrincipal(final Principal resolvedPrincipal, final OAuthRegisteredService registeredService,
                                                    final WebApplicationService service, final CallContext callContext) throws Throwable {
        val accessTokenFactory = (OAuth20AccessTokenFactory) ticketFactory.get(OAuth20AccessToken.class);
        val scopes = requestParameterResolver.resolveRequestedScopes(callContext.webContext());
        val responseType = requestParameterResolver.resolveResponseType(callContext.webContext());
        val grantType = requestParameterResolver.resolveGrantType(callContext.webContext());

        val authentication = DefaultAuthenticationBuilder.newInstance(resolvedPrincipal).build();
        val accessToken = accessTokenFactory.create(service, authentication, scopes,
            registeredService.getClientId(), responseType, grantType);
        val finalPrincipal = profileScopeToAttributesFilter.filter(service, resolvedPrincipal, registeredService, accessToken);
        LOGGER.debug("Built final principal [{}]", finalPrincipal);
        return finalPrincipal;
    }

    protected void validateCredentials(final UsernamePasswordCredentials credentials,
                                       final OAuthRegisteredService registeredService,
                                       final CallContext callContext) {
        if (!clientSecretValidator.validate(registeredService, credentials.getPassword())) {
            throw new CredentialsException("Invalid client credentials provided for registered service: " + registeredService.getName());
        }
    }

    protected boolean canAuthenticate(final CallContext callContext) {
        val context = callContext.webContext();
        val grantType = requestParameterResolver.resolveGrantType(context);

        if (grantType == OAuth20GrantTypes.PASSWORD) {
            LOGGER.debug("Skipping client credential authentication to use password authentication");
            return false;
        }

        val clientIdAndSecret = requestParameterResolver.resolveClientIdAndClientSecret(callContext);
        if (grantType == OAuth20GrantTypes.REFRESH_TOKEN
            && StringUtils.isNotBlank(clientIdAndSecret.getKey())
            && StringUtils.isBlank(clientIdAndSecret.getValue())) {
            LOGGER.debug("Skipping client credential authentication to use refresh token authentication");
            return false;
        }

        val code = context.getRequestParameter(OAuth20Constants.CODE);
        if (code.isPresent()) {
            LOGGER.debug("Checking if the OAuth code issued contains code challenge");
            val token = FunctionUtils.doAndHandle(() -> {
                val state = ticketRegistry.getTicket(code.get(), OAuth20Code.class);
                return state == null || state.isExpired() ? null : state;
            });

            if (token != null && StringUtils.isNotEmpty(token.getCodeChallenge())) {
                LOGGER.debug("The OAuth code [{}] issued contains code challenge which requires PKCE Authentication", code.get());
                return false;
            }
        }
        return true;
    }
}
