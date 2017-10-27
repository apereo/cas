package org.apereo.cas.support.rest.factory;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class DefaultTicketGrantingTicketResourceEntityResponseFactory implements TicketGrantingTicketResourceEntityResponseFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultTicketGrantingTicketResourceEntityResponseFactory.class);

    private static final String DOCTYPE_AND_TITLE = "<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\"><html><head><title>";
    private static final String CLOSE_TITLE_AND_OPEN_FORM = "</title></head><body><h1>TGT Created</h1><form action=\"";
    private static final String TGT_CREATED_TITLE_CONTENT = HttpStatus.CREATED.toString() + ' ' + HttpStatus.CREATED.getReasonPhrase();
    private static final String DOCTYPE_AND_OPENING_FORM = DOCTYPE_AND_TITLE + TGT_CREATED_TITLE_CONTENT + CLOSE_TITLE_AND_OPEN_FORM;
    private static final String REST_OF_THE_FORM_AND_CLOSING_TAGS = "\" method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\"><br><input "
            + "type=\"submit\" value=\"Submit\"></form></body></html>";
    private static final int SUCCESSFUL_TGT_CREATED_INITIAL_LENGTH = DOCTYPE_AND_OPENING_FORM.length() + REST_OF_THE_FORM_AND_CLOSING_TAGS.length();


    @Override
    public ResponseEntity<String> build(final TicketGrantingTicket ticketGrantingTicket, final HttpServletRequest request) throws Exception {
        final URI ticketReference = new URI(request.getRequestURL().toString() + '/' + ticketGrantingTicket.getId());
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ticketReference);
        final String response;
        if (isDefaultContentType(request)) {
            headers.setContentType(MediaType.TEXT_HTML);
            final String tgtUrl = ticketReference.toString();
            response = new StringBuilder(SUCCESSFUL_TGT_CREATED_INITIAL_LENGTH + tgtUrl.length())
                    .append(DOCTYPE_AND_OPENING_FORM)
                    .append(tgtUrl)
                    .append(REST_OF_THE_FORM_AND_CLOSING_TAGS)
                    .toString();
        } else {
            response = ticketGrantingTicket.getId();
        }
        final ResponseEntity<String> entity = new ResponseEntity<>(response, headers, HttpStatus.CREATED);
        LOGGER.debug("Created response entity [{}]", entity);
        return entity;
    }

    private boolean isDefaultContentType(final HttpServletRequest request) {
        final String accept = request.getHeader(HttpHeaders.ACCEPT) == null ? null : request.getHeader(HttpHeaders.ACCEPT).trim();
        return StringUtils.isBlank(accept) || accept.startsWith(MediaType.ALL_VALUE) || accept.startsWith(MediaType.TEXT_HTML_VALUE);
    }

}
