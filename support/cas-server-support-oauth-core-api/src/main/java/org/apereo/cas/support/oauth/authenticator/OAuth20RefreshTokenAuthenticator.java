package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.exception.CredentialsException;

import java.io.Serializable;

/**
 * This is {@link OAuth20RefreshTokenAuthenticator}.
 *
 * {@link OAuth20RefreshTokenAuthenticator} can only be used for a refresh token request of a "public" client.
 * An OAuth "public" client is one that does not define a secret like a mobile application.
 *
 * @author Julien Huon
 * @since 6.2.0
 */
@Slf4j
public class OAuth20RefreshTokenAuthenticator extends OAuth20ClientIdClientSecretAuthenticator {

    public OAuth20RefreshTokenAuthenticator(final ServicesManager servicesManager,
                                            final ServiceFactory webApplicationServiceFactory,
                                            final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                            final TicketRegistry ticketRegistry,
                                            final CipherExecutor<Serializable, String> registeredServiceCipherExecutor,
                                            final PrincipalResolver principalResolver) {
        super(servicesManager, webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer,
            registeredServiceCipherExecutor, ticketRegistry, principalResolver);
    }

    /**
     * Verify if OAuth20RefreshTokenAuthenticator can authenticate the given context.
     *
     * @param context the context
     * @return true if authenticator can validate credentials.
     */
    @Override
    protected boolean canAuthenticate(final WebContext context) {
        val grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);
        val clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID);

        if (clientId.isPresent() && grantType.isPresent()
            && OAuth20Utils.isGrantType(grantType.get(), OAuth20GrantTypes.REFRESH_TOKEN)
            && context.getRequestParameter(OAuth20Constants.REFRESH_TOKEN).isPresent()) {
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getServicesManager(), clientId.get());

            LOGGER.trace("Checking if the client [{}] is eligible for refresh token authentication", clientId.get());
            if (registeredService != null && !OAuth20Utils.doesServiceNeedAuthentication(registeredService)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void validateCredentials(final UsernamePasswordCredentials credentials,
                                       final OAuthRegisteredService registeredService, final WebContext context) {
        val token = credentials.getPassword();
        LOGGER.trace("Received refresh token [{}] for authentication", token);

        val refreshToken = getTicketRegistry().getTicket(token, OAuth20RefreshToken.class);
        val clientId = credentials.getUsername();
        if (refreshToken == null || refreshToken.isExpired() || !StringUtils.equals(refreshToken.getClientId(), clientId)) {
            LOGGER.error("Refresh token [{}] is either not found in the ticket registry, has expired or is not related to the client [{}]", token, clientId);
            throw new CredentialsException("Invalid token: " + token);
        }
    }
}
