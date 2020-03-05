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
 * @author Julien Huon
 * @since 6.1.6
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

    @Override
    protected boolean canAuthenticate(final WebContext context) {
        val grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);

        if (grantType.isPresent() && OAuth20Utils.isGrantType(grantType.get(), OAuth20GrantTypes.REFRESH_TOKEN)) {
            return context.getRequestParameter(OAuth20Constants.REFRESH_TOKEN).isPresent();
        }
        return false;
    }

    @Override
    protected void validateCredentials(final UsernamePasswordCredentials credentials,
                                       final OAuthRegisteredService registeredService, final WebContext context) {
        val clientId = OAuth20Utils.getClientIdAndClientSecret(context).getLeft();
        val clientSecret = OAuth20Utils.getClientIdAndClientSecret(context).getRight();

        if (!OAuth20Utils.checkClientSecret(registeredService, clientSecret, getRegisteredServiceCipherExecutor())) {
            throw new CredentialsException("Client Credentials provided is not valid for registered service: " + registeredService.getName());
        }

        val token = context.getRequestParameter(OAuth20Constants.REFRESH_TOKEN)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        LOGGER.trace("Received refresh token [{}] for authentication", token);

        val refreshToken = getTicketRegistry().getTicket(token, OAuth20RefreshToken.class);
        if (refreshToken == null || refreshToken.isExpired() || !StringUtils.equals(refreshToken.getClientId(), clientId)) {
            LOGGER.error("Provided refresh token [{}] is either not found in the ticket registry or has expired or not related with the client provided");
            throw new CredentialsException("Invalid token: " + token);
        }
    }
}
