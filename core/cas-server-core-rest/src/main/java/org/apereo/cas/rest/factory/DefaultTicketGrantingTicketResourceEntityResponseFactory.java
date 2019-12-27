package org.apereo.cas.rest.factory;

import org.apereo.cas.ticket.TicketGrantingTicket;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

import java.net.URI;

/**
 * This is {@link DefaultTicketGrantingTicketResourceEntityResponseFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class DefaultTicketGrantingTicketResourceEntityResponseFactory implements TicketGrantingTicketResourceEntityResponseFactory {
    private static final String DOCTYPE_AND_TITLE = "<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\"><html><head><title>";

    private static final String CLOSE_TITLE_AND_OPEN_FORM = "</title></head><body><h1>TGT Created</h1><form action=\"";

    private static final String TGT_CREATED_TITLE_CONTENT = HttpStatus.CREATED.toString();

    private static final String DOCTYPE_AND_OPENING_FORM = DOCTYPE_AND_TITLE + TGT_CREATED_TITLE_CONTENT + CLOSE_TITLE_AND_OPEN_FORM;

    private static final String REST_OF_THE_FORM_AND_CLOSING_TAGS = "\" method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\"><br><input "
        + "type=\"submit\" value=\"Submit\"></form></body></html>";

    private static String getResponse(final TicketGrantingTicket ticketGrantingTicket,
                                      final HttpServletRequest request, final URI ticketReference,
                                      final HttpHeaders headers) {
        if (isDefaultContentType(request)) {
            headers.setContentType(MediaType.TEXT_HTML);
            val tgtUrl = ticketReference.toString();
            return DOCTYPE_AND_OPENING_FORM
                + tgtUrl
                + REST_OF_THE_FORM_AND_CLOSING_TAGS;
        }
        return ticketGrantingTicket.getId();
    }

    private static boolean isDefaultContentType(final HttpServletRequest request) {
        val header = request.getHeader(HttpHeaders.ACCEPT);
        val accept = StringUtils.defaultString(header);
        return StringUtils.isBlank(accept) || accept.startsWith(MediaType.ALL_VALUE) || accept.startsWith(MediaType.TEXT_HTML_VALUE);
    }

    @Audit(
        action = "REST_API_TICKET_GRANTING_TICKET",
        actionResolverName = "REST_API_TICKET_GRANTING_TICKET_ACTION_RESOLVER",
        resourceResolverName = "REST_API_TICKET_GRANTING_TICKET_RESOURCE_RESOLVER")
    @Override
    public ResponseEntity<String> build(final TicketGrantingTicket ticketGrantingTicket, final HttpServletRequest request) throws Exception {
        val ticketReference = new URI(request.getRequestURL().toString() + '/' + ticketGrantingTicket.getId());
        val headers = new HttpHeaders();
        headers.setLocation(ticketReference);
        val response = getResponse(ticketGrantingTicket, request, ticketReference, headers);
        val entity = new ResponseEntity<String>(response, headers, HttpStatus.CREATED);
        LOGGER.debug("Created response entity [{}]", entity);
        return entity;
    }

}
