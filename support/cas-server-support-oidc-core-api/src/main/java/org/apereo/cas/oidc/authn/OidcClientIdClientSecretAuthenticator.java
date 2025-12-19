package org.apereo.cas.oidc.authn;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.oidc.discovery.OidcServerDiscoverySettings;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20ClientAuthenticationMethods;
import org.apereo.cas.support.oauth.authenticator.OAuth20ClientIdClientSecretAuthenticator;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.registry.TicketRegistry;
import lombok.val;
import org.pac4j.core.context.CallContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link OidcClientIdClientSecretAuthenticator}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class OidcClientIdClientSecretAuthenticator extends OAuth20ClientIdClientSecretAuthenticator {
    private final OidcServerDiscoverySettings oidcServerDiscoverySettings;

    public OidcClientIdClientSecretAuthenticator(final ServicesManager servicesManager,
                                                 final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
                                                 final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                 final TicketRegistry ticketRegistry,
                                                 final PrincipalResolver principalResolver,
                                                 final OAuth20RequestParameterResolver requestParameterResolver,
                                                 final OAuth20ClientSecretValidator clientSecretValidator,
                                                 final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
                                                 final TicketFactory ticketFactory,
                                                 final ConfigurableApplicationContext applicationContext,
                                                 final OidcServerDiscoverySettings oidcServerDiscoverySettings) {
        super(servicesManager, webApplicationServiceServiceFactory, registeredServiceAccessStrategyEnforcer,
            ticketRegistry, principalResolver, requestParameterResolver,
            clientSecretValidator, profileScopeToAttributesFilter,
            ticketFactory, applicationContext);
        this.oidcServerDiscoverySettings = oidcServerDiscoverySettings;
    }

    @Override
    protected boolean isAuthenticationMethodSupported(final CallContext callContext, final OAuthRegisteredService registeredService,
                                                      final OAuth20ClientAuthenticationMethods requiredAuthnMethod) {
        val authMethodSupported = oidcServerDiscoverySettings.getTokenEndpointAuthMethodsSupported()
            .stream()
            .map(OAuth20ClientAuthenticationMethods::parse)
            .anyMatch(method -> method == requiredAuthnMethod);
        return super.isAuthenticationMethodSupported(callContext, registeredService, requiredAuthnMethod) && authMethodSupported;
    }
}
