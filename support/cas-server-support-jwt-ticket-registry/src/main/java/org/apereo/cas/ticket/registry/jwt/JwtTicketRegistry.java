package org.apereo.cas.ticket.registry.jwt;

import com.google.common.collect.Lists;
import com.nimbusds.jwt.JWTClaimsSet;
import org.apache.commons.codec.binary.StringUtils;
import org.apereo.cas.ticket.BaseTicketSerializers;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.AbstractTicketRegistry;
import org.apereo.cas.web.support.CookieRetrievingCookieGenerator;
import org.apereo.cas.web.support.WebUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;


/**
 * This is {@link JwtTicketRegistry}, which manages and issues
 * CAS tickets in form of JWTs. This registry is intended
 * to be used for stateless CAS deployments as there is no underlying
 * cache/storage backing the JWTs.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class JwtTicketRegistry extends AbstractTicketRegistry {

    private final CookieRetrievingCookieGenerator cookieRetrievingCookieGenerator;

    /**
     * Instantiates a new Infinispan ticket registry.
     *
     * @param cookieGenerator the cookie generator
     */
    public JwtTicketRegistry(final CookieRetrievingCookieGenerator cookieGenerator) {
        this.cookieRetrievingCookieGenerator = cookieGenerator;
    }

    /**
     * Init.
     */
    @PostConstruct
    public void init() {
        logger.info("Setting up JWT Ticket Registry...");
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        return ticket;
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        logger.debug("addTicket() is not supported by {}", getClass().getSimpleName());
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        try {
            logger.debug("Decoding ticket id {}", ticketId);
            final String encTicketId = this.cipherExecutor.decode(ticketId).toString();

            logger.debug("Decoded ticket {}. Parsing claims...", encTicketId);
            final JWTClaimsSet claims = JWTClaimsSet.parse(encTicketId);

            logger.debug("Located ticket id {} from claims", encTicketId);
            final String realTicketId = this.cipherExecutor.decode(claims.getJWTID()).toString();

            logger.debug("Reconstructing ticket {} from claims...", realTicketId);
            final String type = claims.getStringClaim(JwtTicketClaims.TYPE);

            final String ticketContent = claims.getStringClaim(JwtTicketClaims.CONTENT_BODY);
            final Ticket ticket = BaseTicketSerializers.deserializeTicket(ticketContent, type);

            logger.debug("Recreated ticket instance {}. Validating...", ticket.getId());
            validateTicketBasedOnClaims(ticket, claims);

            return ticket;
        } catch (final Exception e) {
            logger.warn(e.getMessage(), e);
        }

        return null;
    }

    private void validateTicketBasedOnClaims(final Ticket ticket, final JWTClaimsSet claims) {
        final String realTicketId = this.cipherExecutor.decode(claims.getJWTID()).toString();
        final String ticketIdFromObject = this.cipherExecutor.decode(ticket.getId()).toString();
        if (!StringUtils.equals(realTicketId, ticketIdFromObject)) {
            throw new InvalidTicketException(new RuntimeException("Ticket id " + realTicketId + " located from claim does not match actual ticket"),
                    realTicketId);
        }
        if (claims.getAudience().isEmpty()) {
            throw new InvalidTicketException(new RuntimeException("Ticket " + realTicketId + " has not defined an audience"), realTicketId);
        }
        if (claims.getIssuer().isEmpty()) {
            throw new InvalidTicketException(new RuntimeException("Ticket " + realTicketId + " has not defined an issuer"), realTicketId);
        }
        if (ticket.isExpired()) {
            throw new InvalidTicketException(new RuntimeException("Ticket " + realTicketId + " has expired"), realTicketId);
        }
    }

    @Override
    public boolean deleteSingleTicket(final String ticketId) {
        final HttpServletResponse response = WebUtils.getHttpServletResponseFromRequestAttributes();
        if (response != null) {
            this.cookieRetrievingCookieGenerator.removeCookie(response);
        }
        return true;
    }

    @Override
    public Collection<Ticket> getTickets() {
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromRequestAttributes();
        if (request != null) {
            final String value = this.cookieRetrievingCookieGenerator.retrieveCookieValue(request);
            final Ticket ticket = getTicket(value);
            return Lists.newArrayList(ticket);
        }
        return Lists.newArrayList();
    }

}
