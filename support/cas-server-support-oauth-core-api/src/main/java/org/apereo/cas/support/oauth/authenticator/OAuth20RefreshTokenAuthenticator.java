package org.apereo.cas.support.oauth.authenticator;

import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.authentication.principal.ServiceFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.profile.OAuth20ProfileScopeToAttributesFilter;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.support.oauth.validator.OAuth20ClientSecretValidator;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.Strings;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.exception.CredentialsException;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * This is {@link OAuth20RefreshTokenAuthenticator}.
 * <p>
 * {@link OAuth20RefreshTokenAuthenticator} can only be used for a refresh token request of a "public" client.
 * An OAuth "public" client is one that does not define a secret like a mobile application.
 *
 * @author Julien Huon
 * @since 6.2.0
 */
@Slf4j
public class OAuth20RefreshTokenAuthenticator extends OAuth20ClientIdClientSecretAuthenticator {

    public OAuth20RefreshTokenAuthenticator(
        final ServicesManager servicesManager,
        final ServiceFactory webApplicationServiceFactory,
        final AuditableExecution registeredServiceAccessStrategyEnforcer,
        final TicketRegistry ticketRegistry,
        final PrincipalResolver principalResolver,
        final OAuth20RequestParameterResolver requestParameterResolver,
        final OAuth20ClientSecretValidator clientSecretValidator,
        final OAuth20ProfileScopeToAttributesFilter profileScopeToAttributesFilter,
        final TicketFactory ticketFactory,
        final ConfigurableApplicationContext applicationContext) {
        super(servicesManager, webApplicationServiceFactory, registeredServiceAccessStrategyEnforcer,
            ticketRegistry, principalResolver, requestParameterResolver, clientSecretValidator,
            profileScopeToAttributesFilter, ticketFactory, applicationContext);
    }

    @Override
    protected boolean canAuthenticate(final CallContext callContext) {
        val context = callContext.webContext();
        val grantType = context.getRequestParameter(OAuth20Constants.GRANT_TYPE);
        val clientId = context.getRequestParameter(OAuth20Constants.CLIENT_ID);

        if (clientId.isPresent() && grantType.isPresent()
            && OAuth20Utils.isGrantType(grantType.get(), OAuth20GrantTypes.REFRESH_TOKEN)
            && context.getRequestParameter(OAuth20Constants.REFRESH_TOKEN).isPresent()) {
            val registeredService = OAuth20Utils.getRegisteredOAuthServiceByClientId(getServicesManager(), clientId.get());

            LOGGER.trace("Checking if the client [{}] is eligible for refresh token authentication", clientId.get());
            return registeredService != null && !OAuth20Utils.doesServiceNeedAuthentication(registeredService);
        }
        return false;
    }

    @Override
    protected void validateCredentials(final UsernamePasswordCredentials credentials,
                                       final OAuthRegisteredService registeredService,
                                       final CallContext callContext) {
        val token = credentials.getPassword();
        LOGGER.trace("Received refresh token [{}] for authentication", token);

        val refreshToken = FunctionUtils.doAndHandle(() -> {
            val state = getTicketRegistry().getTicket(token, OAuth20RefreshToken.class);
            return state == null || state.isExpired() ? null : state;
        });
        val clientId = credentials.getUsername();
        if (refreshToken == null || refreshToken.isExpired() || !Strings.CI.equals(refreshToken.getClientId(), clientId)) {
            LOGGER.error("Refresh token [{}] is either not found in the ticket registry, has expired or does not belong to the client [{}]", token, clientId);
            throw new CredentialsException("Invalid token: " + token);
        }
    }
}
