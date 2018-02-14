package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.authenticator.OAuth20CasAuthenticationBuilder;
import org.apereo.cas.ticket.registry.TicketRegistry;

/**
 * This is {@link AccessTokenClientCredentialsGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AccessTokenClientCredentialsGrantRequestExtractor extends AccessTokenPasswordGrantRequestExtractor {


    public AccessTokenClientCredentialsGrantRequestExtractor(final ServicesManager servicesManager,
                                                             final TicketRegistry ticketRegistry,
                                                             final OAuth20CasAuthenticationBuilder authenticationBuilder,
                                                             final CentralAuthenticationService centralAuthenticationService,
                                                             final OAuthProperties oAuthProperties,
                                                             final AuditableExecution registeredServiceAccessStrategyEnforcer) {
        super(servicesManager, ticketRegistry, authenticationBuilder,
            centralAuthenticationService, oAuthProperties, registeredServiceAccessStrategyEnforcer);
    }

    @Override
    public OAuth20GrantTypes getGrantType() {
        return OAuth20GrantTypes.CLIENT_CREDENTIALS;
    }
}
