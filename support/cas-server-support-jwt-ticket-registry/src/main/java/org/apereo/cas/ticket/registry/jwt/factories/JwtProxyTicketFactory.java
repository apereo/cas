package org.apereo.cas.ticket.registry.jwt.factories;

import com.google.common.base.Throwables;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.BaseTicketSerializers;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.factory.DefaultProxyTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyTicket;
import org.apereo.cas.ticket.registry.jwt.JwtTicketClaims;
import org.apereo.cas.util.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is {@link JwtProxyTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JwtProxyTicketFactory extends DefaultProxyTicketFactory {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected <T extends Ticket> T produceTicket(final ProxyGrantingTicket proxyGrantingTicket, final Service service,
                                                 final String ticketId) {
        try {
            final ProxyTicket pt = super.produceTicket(proxyGrantingTicket, service, ticketId);
            final JWTClaimsSet.Builder claims =
                    new JWTClaimsSet.Builder()
                            .audience(casProperties.getServer().getPrefix())
                            .issuer(casProperties.getServer().getPrefix())
                            .jwtID(pt.getId())
                            .claim(JwtTicketClaims.CONTENT_BODY, BaseTicketSerializers.serializeTicket(pt))
                            .claim(JwtTicketClaims.TYPE, ProxyGrantingTicket.class.getName())
                            .issueTime(DateTimeUtils.dateOf(proxyGrantingTicket.getCreationTime()))
                            .subject(proxyGrantingTicket.getAuthentication().getPrincipal().getId());

            final JWTClaimsSet claimsSet = claims.build();
            final JSONObject object = claimsSet.toJSONObject();
            final String id = this.cipherExecutor.encode(object.toString());
            return super.produceTicket(proxyGrantingTicket, service, id);
        } catch (final Exception e) {
            throw new InvalidTicketException(Throwables.propagate(e), ticketId);
        }
    }
}
