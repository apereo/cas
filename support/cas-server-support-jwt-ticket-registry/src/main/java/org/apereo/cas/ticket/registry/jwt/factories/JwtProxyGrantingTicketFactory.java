package org.apereo.cas.ticket.registry.jwt.factories;

import com.google.common.base.Throwables;
import com.nimbusds.jwt.JWTClaimsSet;
import net.minidev.json.JSONObject;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.BaseTicketSerializers;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.ProxyGrantingTicketImpl;
import org.apereo.cas.ticket.ServiceTicket;
import org.apereo.cas.ticket.factory.DefaultProxyGrantingTicketFactory;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.util.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is {@link JwtProxyGrantingTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JwtProxyGrantingTicketFactory extends DefaultProxyGrantingTicketFactory {
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected <T extends ProxyGrantingTicket> T produceTicket(final ServiceTicket serviceTicket,
                                                              final Authentication authentication, final String ticketId) {
        try {
            final ProxyGrantingTicket pgt = super.produceTicket(serviceTicket, authentication, ticketId);
            final JWTClaimsSet.Builder claims =
                    new JWTClaimsSet.Builder()
                            .audience(serviceTicket.getService().getId())
                            .issuer(casProperties.getServer().getPrefix())
                            .jwtID(pgt.getId())
                            .claim(JwtTicketClaims.CONTENT_BODY, BaseTicketSerializers.serializeTicket(pgt))
                            .claim(JwtTicketClaims.TYPE, ProxyGrantingTicket.class.getName())
                            .issueTime(DateTimeUtils.dateOf(authentication.getAuthenticationDate()))
                            .subject(authentication.getPrincipal().getId());

            final JWTClaimsSet claimsSet = claims.build();
            final JSONObject object = claimsSet.toJSONObject();
            final String id = this.cipherExecutor.encode(object.toString());
            return (T) new ProxyGrantingTicketImpl(id, serviceTicket.getService(),
                    pgt.getGrantingTicket(), authentication, pgt.getExpirationPolicy());
        } catch (final Exception e) {
            throw new InvalidTicketException(Throwables.propagate(e), ticketId);
        }
    }
}
