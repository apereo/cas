/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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
package org.jasig.cas.extension.rest;

import org.apache.commons.io.IOUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.InvalidTicketException;
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
 * Even though the default URI for tickets resource is {@code /v1/tickets}
 * it could be re-configured to whatever URI is desired by setting the following properties
 * in either classpath-based or externalized cas.properties file:
 *
 * <ul>
 *     <li>{@code server.rest.prefix=/v1}</li>
 *     <li>{@code server.rest.ticketsResource.name=tickets}</li>
 * </ul>
 *
 * @author Dmitriy Kopylenko
 * @author Unicon inc.
 *
 * @since 4.1
 */
@RestController("${server.rest.prefix}:/v1")
public class TicketsResource {

    @Autowired
    private CentralAuthenticationService cas;

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketsResource.class);

    /**
     * Create new ticket granting ticket.
     *
     * @param credentials username and password application/x-www-form-urlencoded values
     * @param request raw HttpServletRequest used to call this method
     * @return ResponseEntity representing RESTful response
     */
    @RequestMapping(value = "/${server.rest.ticketsResource.name:tickets}",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<String> createTicketGrantingTicket(@RequestBody final MultiValueMap<String, String> credentials,
                                                      final HttpServletRequest request) {

        Formatter fmt = null;
        try {
            final String tgtId = this.cas.createTicketGrantingTicket(
                    new UsernamePasswordCredential(credentials.getFirst("username"), credentials.getFirst("password")));

            final URI ticketReference = new URI(request.getRequestURL().toString() + "/" + tgtId);
            final HttpHeaders headers = new HttpHeaders();
            headers.setLocation(ticketReference);
            headers.setContentType(MediaType.TEXT_HTML);

            fmt = new Formatter();
            fmt.format("<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\"><html><head><title>");
            fmt.format("%s %s", HttpStatus.CREATED, HttpStatus.CREATED.getReasonPhrase())
                    .format("</title></head><body><h1>TGT Created</h1><form action=\"%s", ticketReference.toString())
                    .format("\" method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\">")
                    .format("<br><input type=\"submit\" value=\"Submit\"></form></body></html>");

            return new ResponseEntity<String>(fmt.toString(), headers, HttpStatus.CREATED);
        }
        catch (final Throwable e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
        finally {
            IOUtils.closeQuietly(fmt);
        }
    }

    /**
     * Create new service ticket.
     *
     * @param request service application/x-www-form-urlencoded value
     * @param tgtId ticket granting ticket id URI path param
     * @return @return ResponseEntity representing RESTful response
     */
    @RequestMapping(value = "/${server.rest.ticketsResource.name:tickets}/{tgtId:.+}",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    ResponseEntity<String> createServiceTicket(@RequestBody final MultiValueMap<String, String> request, @PathVariable("tgtId") final String tgtId) {
        try {
            final String serviceTicketId = this.cas.grantServiceTicket(tgtId, new SimpleWebApplicationServiceImpl(request.getFirst("service")));
            return new ResponseEntity<String>(serviceTicketId, HttpStatus.OK);
        }
        catch (final InvalidTicketException e) {
            return new ResponseEntity<String>("TicketGrantingTicket could not be found", HttpStatus.NOT_FOUND);
        }
        catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Destroy ticket granting ticket.
     *
     * @param tgtId ticket granting ticket id URI path param
     */
    @RequestMapping(value = "/${server.rest.ticketsResource.name:tickets}/{tgtId:.+}", method = RequestMethod.DELETE)
    void deleteTicketGrantingTicket(@PathVariable("tgtId") final String tgtId) {
        this.cas.destroyTicketGrantingTicket(tgtId);
    }
}
