/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.support.rest;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.Formatter;

/**
 * {@link org.springframework.web.bind.annotation.RestController} implementation of CAS' REST API.
 *
 * This class implements main CAS RESTful resource for vending/deleting TGTs and vending STs:
 *
 * <ul>
 *     <li>{@code POST /v1/tickets}</li>
 *     <li>{@code POST /v1/tickets/{TGT-id}}</li>
 *     <li>{@code DELETE /v1/tickets/{TGT-id}}</li>
 * </ul>
 *
 * @author Dmitriy Kopylenko
 * @since 4.1.0
 */
@RestController("/v1")
public class TicketsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketsResource.class);

    @Autowired
    private CentralAuthenticationService cas;

    @Autowired(required = false)
    private CredentialFactory credentialFactory = new DefaultCredentialFactory();

    /**
     * Create new ticket granting ticket.
     *
     * @param requestBody username and password application/x-www-form-urlencoded values
     * @param request raw HttpServletRequest used to call this method
     * @return ResponseEntity representing RESTful response
     */
    @RequestMapping(value = "/tickets", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public final ResponseEntity<String> createTicketGrantingTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                                   final HttpServletRequest request) {
        try (Formatter fmt = new Formatter()) {
            final TicketGrantingTicket tgtId = this.cas.createTicketGrantingTicket(this.credentialFactory.fromRequestBody(requestBody));
            final URI ticketReference = new URI(request.getRequestURL().toString() + '/' + tgtId.getId());
            final HttpHeaders headers = new HttpHeaders();
            headers.setLocation(ticketReference);
            headers.setContentType(MediaType.TEXT_HTML);
            fmt.format("<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\"><html><head><title>");
            //IETF//DTD HTML 2.0//EN\\\"><html><head><title>");
            fmt.format("%s %s", HttpStatus.CREATED, HttpStatus.CREATED.getReasonPhrase())
                    .format("</title></head><body><h1>TGT Created</h1><form action=\"%s", ticketReference.toString())
                    .format("\" method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\">")
                    .format("<br><input type=\"submit\" value=\"Submit\"></form></body></html>");
            return new ResponseEntity<String>(fmt.toString(), headers, HttpStatus.CREATED);
        } catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Create new service ticket.
     *
     * @param requestBody service application/x-www-form-urlencoded value
     * @param tgtId ticket granting ticket id URI path param
     * @return {@link ResponseEntity} representing RESTful response
     */
    @RequestMapping(value = "/tickets/{tgtId:.+}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public final ResponseEntity<String> createServiceTicket(@RequestBody final MultiValueMap<String, String> requestBody,
                                                            @PathVariable("tgtId") final String tgtId) {
        try {
            final ServiceTicket serviceTicketId = this.cas.grantServiceTicket(tgtId,
                    new SimpleWebApplicationServiceImpl(requestBody.getFirst("service")));
            return new ResponseEntity<String>(serviceTicketId.getId(), HttpStatus.OK);
        } catch (final InvalidTicketException e) {
            return new ResponseEntity<String>("TicketGrantingTicket could not be found", HttpStatus.NOT_FOUND);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Destroy ticket granting ticket.
     *
     * @param tgtId ticket granting ticket id URI path param
     * @return {@link ResponseEntity} representing RESTful response. Signals
     * {@link HttpStatus#OK} when successful.
     */
    @RequestMapping(value = "/tickets/{tgtId:.+}", method = RequestMethod.DELETE)
    public final ResponseEntity<String> deleteTicketGrantingTicket(@PathVariable("tgtId") final String tgtId) {
        this.cas.destroyTicketGrantingTicket(tgtId);
        return new ResponseEntity<>(tgtId, HttpStatus.OK);
    }

    /**
     * Default implementation of CredentialFactory.
     */
    public static class DefaultCredentialFactory implements CredentialFactory {

        @Override
        public Credential fromRequestBody(@NotNull final MultiValueMap<String, String> requestBody) {
            return new UsernamePasswordCredential(requestBody.getFirst("username"), requestBody.getFirst("password"));
        }
    }
}
