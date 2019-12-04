package org.apereo.cas.tokens;

import org.apereo.cas.rest.factory.DefaultTicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.token.TokenConstants;
import org.apereo.cas.token.TokenTicketBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link JwtTicketGrantingTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class JwtTicketGrantingTicketResourceEntityResponseFactory extends DefaultTicketGrantingTicketResourceEntityResponseFactory {
    private final TokenTicketBuilder tokenTicketBuilder;

    @Override
    public ResponseEntity<String> build(final TicketGrantingTicket ticketGrantingTicket, final HttpServletRequest request) throws Exception {
        var tokenParam = request.getParameter(TokenConstants.PARAMETER_NAME_TOKEN);
        if (StringUtils.isBlank(tokenParam)) {
            tokenParam = request.getHeader(TokenConstants.PARAMETER_NAME_TOKEN);
        }
        if (StringUtils.isBlank(tokenParam) || !BooleanUtils.toBoolean(tokenParam)) {
            LOGGER.debug("The request indicates that ticket-granting ticket should not be created as a JWT");
            return super.build(ticketGrantingTicket, request);
        }

        val jwt = this.tokenTicketBuilder.build(ticketGrantingTicket);
        LOGGER.debug("Generated JWT [{}]", jwt);

        val headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        val entity = new ResponseEntity<String>(jwt, headers, HttpStatus.CREATED);
        LOGGER.debug("Created response entity [{}]", entity);
        return entity;

    }
}
