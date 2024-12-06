package org.apereo.cas.oidc.ticket;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.ticket.BaseTicketCatalogConfigurer;
import org.apereo.cas.ticket.TicketCatalog;
import org.apereo.cas.ticket.TicketDefinition;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.Ordered;

/**
 * This is {@link OidcTicketCatalogConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Slf4j
public class OidcTicketCatalogConfigurer extends BaseTicketCatalogConfigurer {

    @Override
    public void configureTicketCatalog(final TicketCatalog plan,
                                       final CasConfigurationProperties casProperties) {
        LOGGER.trace("Registering OpenID Connect protocol ticket definitions...");
        buildAndRegisterCibaRequestDefinition(plan, buildTicketDefinition(plan, OidcCibaRequest.PREFIX,
            OidcCibaRequest.class, OidcDefaultCibaRequest.class, Ordered.HIGHEST_PRECEDENCE), casProperties);

        buildAndRegisterPushedAuthorizationRequestDefinition(plan, buildTicketDefinition(plan,
            OidcPushedAuthorizationRequest.PREFIX, OidcPushedAuthorizationRequest.class, OidcDefaultPushedAuthorizationRequest.class,
            Ordered.HIGHEST_PRECEDENCE), casProperties);
    }

    protected void buildAndRegisterCibaRequestDefinition(final TicketCatalog plan, final TicketDefinition metadata,
                                                         final CasConfigurationProperties casProperties) {
        val ciba = casProperties.getAuthn().getOidc().getCiba();
        metadata.getProperties().setStorageName(ciba.getStorageName());
        val timeout = Beans.newDuration(ciba.getMaxTimeToLiveInSeconds()).toSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        metadata.getProperties().setExcludeFromCascade(casProperties.getTicket().isTrackDescendantTickets());
        registerTicketDefinition(plan, metadata);
    }

    protected void buildAndRegisterPushedAuthorizationRequestDefinition(final TicketCatalog plan, final TicketDefinition metadata,
                                                                        final CasConfigurationProperties casProperties) {
        val par = casProperties.getAuthn().getOidc().getPar();
        metadata.getProperties().setStorageName(par.getStorageName());
        val timeout = Beans.newDuration(par.getMaxTimeToLiveInSeconds()).toSeconds();
        metadata.getProperties().setStorageTimeout(timeout);
        metadata.getProperties().setExcludeFromCascade(casProperties.getTicket().isTrackDescendantTickets());
        registerTicketDefinition(plan, metadata);
    }

}
