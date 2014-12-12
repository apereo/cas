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
package org.jasig.cas.integration.restlet;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.ServiceTicket;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Delete;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Implementation of a Restlet resource for creating Service Tickets from a
 * {@link org.jasig.cas.ticket.TicketGrantingTicket}, as well as deleting a
 * {@link org.jasig.cas.ticket.TicketGrantingTicket}.
 *
 * @author Scott Battaglia
 * @since 3.3
 * @deprecated As of 4.1. Use the {@link TicketResource} implementation from cas-server-support-rest module
 */
@Deprecated
public final class TicketGrantingTicketResource extends ServerResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(TicketGrantingTicketResource.class);

    @Autowired
    private CentralAuthenticationService centralAuthenticationService;

    private String ticketGrantingTicketId;

    @Override
    public void init(final Context context, final Request request, final Response response) {
        super.init(context, request, response);
        this.ticketGrantingTicketId = (String) request.getAttributes().get("ticketGrantingTicketId");
        this.setNegotiated(false);
        this.getVariants().add(new Variant(MediaType.APPLICATION_WWW_FORM));
    }

    /**
     * Removes the TGT.
     */
    @Delete
    public void removeRepresentations() {
        this.centralAuthenticationService.destroyTicketGrantingTicket(this.ticketGrantingTicketId);
        getResponse().setStatus(Status.SUCCESS_OK);
    }

    /**
     * Accept service and attempt to grant service ticket.
     *
     * @param entity the entity
     */
    @Post
    public void acceptRepresentation(final Representation entity) {
        final Form form = new Form(entity);
        final String serviceUrl = form.getFirstValue("service");
        try {
            final ServiceTicket serviceTicketId = this.centralAuthenticationService.grantServiceTicket(
                    this.ticketGrantingTicketId,
                    new SimpleWebApplicationServiceImpl(serviceUrl));
            getResponse().setEntity(serviceTicketId.getId(), MediaType.TEXT_PLAIN);
        } catch (final InvalidTicketException e) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "TicketGrantingTicket could not be found.");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
    }
}
