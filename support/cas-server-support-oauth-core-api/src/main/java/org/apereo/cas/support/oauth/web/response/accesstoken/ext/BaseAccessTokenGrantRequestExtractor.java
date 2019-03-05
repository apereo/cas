package org.apereo.cas.support.oauth.web.response.accesstoken.ext;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.configuration.model.support.oauth.OAuthProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistry;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * This is {@link BaseAccessTokenGrantRequestExtractor}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "ticketTransactionManager")
@RequiredArgsConstructor
public abstract class BaseAccessTokenGrantRequestExtractor implements AccessTokenGrantRequestExtractor {
    /**
     * The Services manager.
     */
    protected final ServicesManager servicesManager;
    /**
     * The Ticket registry.
     */
    protected final TicketRegistry ticketRegistry;

    /**
     * The Services manager.
     */
    protected final CentralAuthenticationService centralAuthenticationService;

    /**
     * OAuth settings.
     */
    protected final OAuthProperties oAuthProperties;
}
