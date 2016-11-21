package org.apereo.cas.ticket.registry.jwt.factories;

import com.google.common.base.Throwables;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.DefaultServiceTicketFactory;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.jwt.serializers.BaseJwtTicketSerializers;
import org.apereo.cas.ticket.registry.jwt.JwtTicketClaims;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * This is {@link JwtServiceTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JwtServiceTicketFactory extends DefaultServiceTicketFactory {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected <T extends Ticket> T produceTicket(final TicketGrantingTicket ticketGrantingTicket, final Service service,
                                                 final boolean credentialProvided, final String ticketId) {
        try {
            final ServiceTicket st = super.produceTicket(ticketGrantingTicket, service, credentialProvided, ticketId);
            final JWTClaimsSet.Builder claims =
                    new JWTClaimsSet.Builder()
                            .audience(casProperties.getServer().getPrefix())
                            .issuer(casProperties.getServer().getPrefix())
                            .jwtID(st.getId())
                            .claim(JwtTicketClaims.CONTENT_BODY, BaseJwtTicketSerializers.serializeTicket(st))
                            .claim(JwtTicketClaims.TYPE, ServiceTicket.class.getName())
                            .issueTime(new Date())
                            .subject(service.getId());
            final JWTClaimsSet claimsSet = claims.build();
            final JSONObject object = claimsSet.toJSONObject();
            final String id = cipherExecutor.encode(object.toJSONString());
            return super.produceTicket(ticketGrantingTicket, service, credentialProvided, id);
        } catch (final Exception e) {
            throw new InvalidTicketException(Throwables.propagate(e), ticketId);
        }
    }
}
