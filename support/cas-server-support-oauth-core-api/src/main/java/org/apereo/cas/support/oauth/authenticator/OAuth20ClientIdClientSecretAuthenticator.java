package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.code.OAuthCode;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.authenticator.Authenticator;
import org.pac4j.core.exception.CredentialsException;
import org.pac4j.core.profile.CommonProfile;

import java.io.Serializable;

/**
 * Authenticator for client credentials authentication.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Slf4j
public class OAuth20ClientIdClientSecretAuthenticator implements Authenticator<UsernamePasswordCredentials> {

    /**
     * {@link TicketRegistry} for storing and retrieving tickets as needed.
     */
    protected final TicketRegistry ticketRegistry;

    /**
     * The ticket cipher, if any.
     */
    protected final CipherExecutor<Serializable, String> registeredServiceCipherExecutor;

    private final ServicesManager servicesManager;
    private final ServiceFactory<WebApplicationService> webApplicationServiceServiceFactory;
    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    public OAuth20ClientIdClientSecretAuthenticator(final ServicesManager servicesManager,
                                                    final ServiceFactory webApplicationServiceFactory,
                                                    final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                    final CipherExecutor<Serializable, String> registeredServiceCipherExecutor,
                                                    final TicketRegistry ticketRegistry) {
        this.servicesManager = servicesManager;
        this.webApplicationServiceServiceFactory = webApplicationServiceFactory;
        this.registeredServiceCipherExecutor = registeredServiceCipherExecutor;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
        this.ticketRegistry = ticketRegistry;
    }


    @Override
    public void validate(final UsernamePasswordCredentials credentials, final WebContext context) throws CredentialsException {
        LOGGER.debug("Authenticating credential [{}]", credentials);

        val id = credentials.getUsername();
        val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(this.servicesManager, id);
        if (registeredService == null) {
            LOGGER.debug("Unable to locate registered service for [{}]", id);
            return;
        }
        if (canAuthenticate(context)) {
            val service = this.webApplicationServiceServiceFactory.createService(registeredService.getServiceId());
            val audit = AuditableContext.builder()
                    .service(service)
                    .registeredService(registeredService)
                    .build();
            val accessResult = this.registeredServiceAccessStrategyEnforcer.execute(audit);
            accessResult.throwExceptionIfNeeded();

            validateCredentials(credentials, registeredService, context);

            val profile = new CommonProfile();
            profile.setId(id);
            credentials.setUserProfile(profile);
            LOGGER.debug("Authenticated user profile [{}]", profile);
        }
    }

    /**
     * Validate credentials.
     *
     * @param credentials       the credentials
     * @param registeredService the registered service
     * @param context           the context
     */
    protected void validateCredentials(final UsernamePasswordCredentials credentials,
                                       final OAuthRegisteredService registeredService,
                                       final WebContext context) {
        if (!OAuth20Utils.checkClientSecret(registeredService, credentials.getPassword(), registeredServiceCipherExecutor)) {
            throw new CredentialsException("Bad secret for client identifier: " + credentials.getPassword());
        }
    }

    /**
     * Check if authentication can be performed for a given context.
     *
     * ClientCredential authentication can be performed if {@code client_id} & {@code client_secret} are provided.
     * Exception to this will be
     * 1. When the grant type is {@code password}, in which case the authentication will be performed by {@code OAuth20UsernamePasswordAuthenticator}
     * 2. When request contains OAuth {@code code} which was issued with a {@code code_challenge}, in which case the authentication will be
     *    performed by {{@code OAuth20ProofKeyCodeExchangeAuthenticator}
     *
     * @param context the context
     * @return true if authenticator can validate credentials.
     *
     * @see <a href="https://tools.ietf.org/html/rfc7636#section-4.3"> PKCE Auth Code Request</a>
     * @see <a href="https://tools.ietf.org/html/rfc7636#section-4.5"> PKCE Token request</a>
     */
    protected boolean canAuthenticate(final WebContext context) {
        val grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);

        if (grantType.isPresent() && OAuth20Utils.isGrantType(grantType.get(), OAuth20GrantTypes.PASSWORD)) {
            LOGGER.debug("Skipping Client credential authentication to use password authentication");
            return false;
        }

        val code = context.getRequestParameter(OAuth20Constants.CODE);

        if (!code.isEmpty()) {
            LOGGER.debug("Checking if the OAuth code issued contains code challenge");
            val token = this.ticketRegistry.getTicket(code.get(), OAuthCode.class);

            if (token != null && token.getCodeChallenge() != null) {
                LOGGER.debug("The OAuth code [{}] issued contains code challenge which requires PKCE Authentication", code.get());
                return false;
            }
        }
        return true;
    }
}
