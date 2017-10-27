package org.apereo.cas.tokens;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.rest.factory.DefaultTicketGrantingTicketResourceEntityResponseFactory;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.token.TokenConstants;
import org.apereo.cas.token.TokenTicketBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

/**
 * This is {@link JWTTicketGrantingTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JWTTicketGrantingTicketResourceEntityResponseFactory extends DefaultTicketGrantingTicketResourceEntityResponseFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWTTicketGrantingTicketResourceEntityResponseFactory.class);
    
    private final ServicesManager servicesManager;

    /**
     * The ticket builder that produces tokens.
     */
    private final TokenTicketBuilder tokenTicketBuilder;
    
    public JWTTicketGrantingTicketResourceEntityResponseFactory(final ServicesManager servicesManager, final TokenTicketBuilder tokenTicketBuilder) {
        this.servicesManager = servicesManager;
        this.tokenTicketBuilder = tokenTicketBuilder;
    }

    @Override
    public ResponseEntity<String> build(final TicketGrantingTicket ticketGrantingTicket, final HttpServletRequest request) throws Exception {
        String tokenParam = request.getParameter(TokenConstants.PARAMETER_NAME_TOKEN);
        if (StringUtils.isBlank(tokenParam)) {
            tokenParam = request.getHeader(TokenConstants.PARAMETER_NAME_TOKEN);    
        }
        if (StringUtils.isBlank(tokenParam) || !BooleanUtils.toBoolean(tokenParam)) {
            LOGGER.debug("The request indicates that ticket-granting ticket should not be created as a JWT");
            return super.build(ticketGrantingTicket, request);
        }
        
        final String jwt = this.tokenTicketBuilder.build(ticketGrantingTicket);
        LOGGER.debug("Generated JWT [{}] for service [{}]", jwt);

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        final ResponseEntity<String> entity = new ResponseEntity<>(jwt, headers, HttpStatus.CREATED);
        LOGGER.debug("Created response entity [{}]", entity);
        return entity;
        
    }
}
