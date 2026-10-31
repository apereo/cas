package org.apereo.cas.support.oauth.authenticator;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
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
import org.apereo.cas.support.oauth.profile.DefaultOAuth20UserProfile;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.profile.OAuth20UserProfile;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.code.OAuth20Code;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.authentication.BadCredentialsException;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Authenticator for client credentials authentication.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class OAuth20ClientIdClientSecretAuthenticator implements OAuth20ClientCredentialsAuthenticator {
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
    public Optional<OAuth20UserProfile> authenticate(final OAuth20ClientAuthenticationToken token) {
        return FunctionUtils.doUnchecked(() -> {
            LOGGER.debug("Authenticating client [{}]", token.getUsername());
            val id = Objects.requireNonNull(token.getUsername());
            val request = token.getRequest();
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(servicesManager, id);
            val audit = AuditableContext.builder()
                .registeredService(registeredService)
                .build();
            val accessResult = registeredServiceAccessStrategyEnforcer.execute(audit);
            val proceed = !accessResult.isExecutionFailure() && canAuthenticate(request);
            if (!proceed) {
                val name = getClass().getSimpleName();
                LOGGER.debug("Skipping authenticator [{}]; service access is rejected for [{}] or the authentication request is not supported", name, registeredService);
                return Optional.<OAuth20UserProfile>empty();
            }

            val requiredAuthnMethod = token.getSource() == OAuth20ClientAuthenticationToken.CredentialSource.FORM
                ? OAuth20ClientAuthenticationMethods.CLIENT_SECRET_POST
                : OAuth20ClientAuthenticationMethods.CLIENT_SECRET_BASIC;
            if (!isAuthenticationMethodSupported(request, Objects.requireNonNull(registeredService), requiredAuthnMethod)) {
                LOGGER.warn("Client authentication method [{}] is not supported for service [{}]", requiredAuthnMethod, registeredService.getName());
                return Optional.<OAuth20UserProfile>empty();
            }

            validateCredentials(token, registeredService, requiredAuthnMethod);

            val resolvedPrincipal = resolvePrincipal(token);
            val service = webApplicationServiceServiceFactory.createService(registeredService.getClientId());
            val profileId = resolvedPrincipal instanceof NullPrincipal
                ? id
                : registeredService.getUsernameAttributeProvider().resolveUsername(RegisteredServiceUsernameProviderContext.builder()
                    .registeredService(registeredService)
                    .service(service)
                    .principal(resolvedPrincipal)
                    .applicationContext(applicationContext)
                    .build());
            val profile = new DefaultOAuth20UserProfile(profileId, OAuth20UserProfile.COMMON_PROFILE_HANDLER_NAME);
            profile.addAttribute(OAuth20Constants.CLIENT_ID, id);
            LOGGER.debug("Created profile id [{}]", profile.getId());

            val principal = buildAuthenticatedPrincipal(resolvedPrincipal, registeredService, service, request);
            profile.addAttributes(principal.getAttributes());

            LOGGER.debug("Authenticated user profile [{}]", profile);
            return Optional.<OAuth20UserProfile>of(profile);
        });
    }

    protected Principal resolvePrincipal(final OAuth20ClientAuthenticationToken token) throws Throwable {
        val credential = new OAuth20ClientIdClientSecretCredential(Objects.requireNonNull(token.getUsername()),
            Objects.requireNonNull((String) token.getCredentials()));
        return Objects.requireNonNull(principalResolver.resolve(credential));
    }

    protected boolean isAuthenticationMethodSupported(final HttpServletRequest request, final OAuthRegisteredService registeredService,
                                                      final OAuth20ClientAuthenticationMethods requiredAuthnMethod) {
        return OAuth20Utils.isTokenAuthenticationMethodSupportedFor(request, registeredService, requiredAuthnMethod);
    }

    protected Principal buildAuthenticatedPrincipal(final Principal resolvedPrincipal, final OAuthRegisteredService registeredService,
                                                    final WebApplicationService service, final HttpServletRequest request)
            throws Throwable {
        val scopes = new LinkedHashSet<>(resolveRequestedScopes(request));
        val oauthCode = requestParameterResolver.resolveRequestParameter(request, OAuth20Constants.CODE)
            .map(code -> OAuth20Utils.getRequestedOAuthToken(request, ticketRegistry, code, OAuth20Token.class))
            .orElse(null);

        if (oauthCode != null && !oauthCode.isExpired()) {
            LOGGER.debug("Found OAuth code [{}] in the ticket registry", oauthCode.getId());
            scopes.addAll(oauthCode.getScopes());
        }
        LOGGER.debug("Filtering principal attributes for service [{}] based on scopes [{}]", service.getId(), scopes);
        val finalPrincipal = profileScopeToAttributesFilter.filter(service, resolvedPrincipal, registeredService, scopes, null);
        LOGGER.debug("Built final principal [{}]", finalPrincipal);
        return finalPrincipal;
    }

    protected Collection<String> resolveRequestedScopes(final HttpServletRequest request) {
        return requestParameterResolver.resolveRequestedScopes(request);
    }

    protected void validateCredentials(final OAuth20ClientAuthenticationToken token,
                                       final OAuthRegisteredService registeredService,
                                       final OAuth20ClientAuthenticationMethods authnMethod) {
        val pwdToCheck = Objects.requireNonNull((String) token.getCredentials());
        if (!clientSecretValidator.validate(registeredService, pwdToCheck)) {
            throw new BadCredentialsException("Invalid client credentials provided for registered service: " + registeredService.getName());
        }
    }

    protected boolean canAuthenticate(final HttpServletRequest request) {
        val grantType = requestParameterResolver.resolveGrantType(request);

        if (grantType == OAuth20GrantTypes.PASSWORD) {
            LOGGER.debug("Skipping client credential authentication to use password authentication");
            return false;
        }

        val clientIdAndSecret = requestParameterResolver.resolveClientIdAndClientSecret(request);
        if (grantType == OAuth20GrantTypes.REFRESH_TOKEN
            && StringUtils.isNotBlank(clientIdAndSecret.getKey())
            && StringUtils.isBlank(clientIdAndSecret.getValue())) {
            LOGGER.debug("Skipping client credential authentication to use refresh token authentication");
            return false;
        }

        val code = Optional.ofNullable(request.getParameter(OAuth20Constants.CODE));
        if (code.isPresent()) {
            LOGGER.debug("Checking if the OAuth code issued contains code challenge");
            val token = OAuth20Utils.getRequestedOAuthToken(request, ticketRegistry, code.get(), OAuth20Code.class);
            if (token != null && !token.isExpired() && StringUtils.isNotEmpty(token.getCodeChallenge())) {
                LOGGER.debug("The OAuth code [{}] issued contains code challenge which requires PKCE Authentication", code.get());
                return false;
            }
        }
        return true;
    }
}
