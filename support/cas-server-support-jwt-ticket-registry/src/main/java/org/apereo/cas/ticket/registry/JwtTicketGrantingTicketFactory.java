package org.apereo.cas.ticket.registry;

import com.google.common.base.Throwables;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.DefaultTicketGrantingTicketFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;
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
        try {
            final Principal principal = authentication.getPrincipal();
            final String tgtId = this.ticketGrantingTicketUniqueTicketIdGenerator.getNewTicketId(TicketGrantingTicket.PREFIX);
            final JWTClaimsSet.Builder claims =
                    new JWTClaimsSet.Builder()
                            .audience(casProperties.getServer().getPrefix())
                            .issuer(casProperties.getServer().getPrefix())
                            .jwtID(tgtId)
                            .issueTime(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
                            .subject(principal.getId());

            authentication.getAttributes().forEach((k, v) -> claims.claim(k, CollectionUtils.toCollection(v)));
            principal.getAttributes().forEach((k, v) -> claims.claim(k, CollectionUtils.toCollection(v)));
            final JWTClaimsSet claimsSet = claims.build();
            final JSONObject object = claimsSet.toJSONObject();
            return cipherExecutor.encode(object.toJSONString());
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
