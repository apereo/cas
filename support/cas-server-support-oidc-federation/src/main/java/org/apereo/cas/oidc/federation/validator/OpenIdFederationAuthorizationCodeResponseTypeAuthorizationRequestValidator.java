package org.apereo.cas.oidc.federation.validator;

import module java.base;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.oidc.federation.chain.OidcFederationResolutionUnavailableException;
import org.apereo.cas.oidc.federation.registration.OidcFederationAutomaticClientRegistration;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.validator.authorization.OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import lombok.extern.slf4j.Slf4j;

/**
 * This is {@link OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator}.
 * Clients are looked up through {@link OidcFederationAutomaticClientRegistration}, so that a relying party of the
 * federation is registered automatically even when the validator runs before
 * {@link org.apereo.cas.oidc.federation.registration.OidcFederationAutomaticRegistrationInterceptor}.
 *
 * @author Jerome LELEU
 * @since 8.1.0
 */
@Slf4j
public class OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator
    extends OAuth20AuthorizationCodeResponseTypeAuthorizationRequestValidator {
    private final OidcFederationAutomaticClientRegistration clientRegistration;

    public OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidator(
        final ServicesManager servicesManager,
        final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory,
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        final OAuth20RequestParameterResolver requestParameterResolver,
        final OidcFederationAutomaticClientRegistration clientRegistration) {
        super(servicesManager, webApplicationServiceServiceFactory, registeredServiceAccessStrategyEnforcer, requestParameterResolver);
        this.clientRegistration = clientRegistration;
    }

    /**
     * The registered service of the client, registering a relying party of the federation on first contact. A relying
     * party that cannot be registered now is not found here; the federation interceptor, which runs first on every
     * authorization endpoint, answers it as temporarily unavailable.
     *
     * @param clientId the client id
     * @return the registered service, or null
     */
    @Override
    protected OAuthRegisteredService getRegisteredServiceByClientId(final String clientId) {
        try {
            return clientRegistration.register(clientId);
        } catch (final OidcFederationResolutionUnavailableException e) {
            LOGGER.debug("Relying party [{}] cannot be registered now: [{}]", clientId, e.getMessage());
            return null;
        }
    }
}
