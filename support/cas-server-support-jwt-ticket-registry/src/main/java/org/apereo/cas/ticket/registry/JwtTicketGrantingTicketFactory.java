package org.apereo.cas.ticket.registry;

import com.nimbusds.jwt.JWTClaimsSet;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.DefaultTicketGrantingTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is {@link JwtTicketGrantingTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JwtTicketGrantingTicketFactory extends DefaultTicketGrantingTicketFactory {

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected String produceTicketIdentifier(final Authentication authentication) {
        final Principal principal = authentication.getPrincipal();
        final String tgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX);
        final JWTClaimsSet.Builder claims =
                new JWTClaimsSet.Builder()
                        .audience(casProperties.getServer().getPrefix())
                        .issuer(casProperties.getServer().getPrefix())
                        .jwtID(tgtId)
                        .issueTime(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
                        .expirationTime(ticketGrantingTicketExpirationPolicy.)
                        .subject(principal.getId());


        final ClientInfo holder = ClientInfoHolder.getClientInfo();
        if (holder != null) {
            claims.claim("origin", holder.getServerIpAddress());
            claims.claim("client", holder.getClientIpAddress());
        }

        authentication.getAttributes().forEach((k, v) -> claims.claim(k, CollectionUtils.toCollection(v));
        principal.getAttributes().forEach((k, v) -> claims.claim(k, CollectionUtils.toCollection(v));

        return super.produceTicketIdentifier(authentication);
    }
}
