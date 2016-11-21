package org.apereo.cas.ticket.registry.jwt.factories;

import com.google.common.base.Throwables;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.DefaultTicketGrantingTicketFactory;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.jwt.serializers.BaseJwtTicketSerializers;
import org.apereo.cas.ticket.registry.jwt.JwtTicketClaims;
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
    protected <T extends TicketGrantingTicket> T produceTicket(final Authentication authentication, final String ticketId) {
        try {
            final TicketGrantingTicket tgt = super.produceTicket(authentication, ticketId);
            final JWTClaimsSet.Builder claims =
                    new JWTClaimsSet.Builder()
                            .audience(casProperties.getServer().getPrefix())
                            .issuer(casProperties.getServer().getPrefix())
                            .jwtID(tgt.getId())
                            .claim(JwtTicketClaims.CONTENT_BODY, BaseJwtTicketSerializers.serializeTicket(tgt))
                            .claim(JwtTicketClaims.TYPE, TicketGrantingTicket.class.getName())
                            .issueTime(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
                            .subject(authentication.getPrincipal().getId());

            final JWTClaimsSet claimsSet = claims.build();
            final JSONObject object = claimsSet.toJSONObject();
            final String id = this.cipherExecutor.encode(object.toString());
            return super.produceTicket(authentication, id);
        } catch (final Exception e) {
            throw new InvalidTicketException(Throwables.propagate(e), ticketId);
        }
    }

}
