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

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.restlet.Request;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.WebRequest;

import java.security.Principal;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Handles the creation of Ticket Granting Tickets.
 *
 * @author Scott Battaglia
 * @since 3.3
 * @deprecated Use TicketsResource implementation from cas-server-support-rest module
 */
@Deprecated
public class TicketResource extends ServerResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicketResource.class);

    @Autowired
    private CentralAuthenticationService centralAuthenticationService;

    /**
     * Instantiates a new ticket resource.
     */
    public TicketResource() {
        setNegotiated(false);
    }

    /**
     * Accept credentials and attempt to create the TGT.
     *
     * @param entity the entity
     */
    @Post
    public final void acceptRepresentation(final Representation entity)  {
        LOGGER.debug("Obtaining credentials...");
        final Credential c = obtainCredentials();

        try (final Formatter fmt = new Formatter()) {
            final TicketGrantingTicket ticketGrantingTicketId = this.centralAuthenticationService.createTicketGrantingTicket(c);
            getResponse().setStatus(determineStatus());
            final Reference ticketReference = getRequest().getResourceRef().addSegment(ticketGrantingTicketId.getId());
            getResponse().setLocationRef(ticketReference);

            fmt.format("<!DOCTYPE HTML PUBLIC \\\"-//IETF//DTD HTML 2.0//EN\\\"><html><head><title>");
            fmt.format("%s %s", getResponse().getStatus().getCode(), getResponse().getStatus().getDescription())
               .format("</title></head><body><h1>TGT Created</h1><form action=\"%s", ticketReference)
               .format("\" method=\"POST\">Service:<input type=\"text\" name=\"service\" value=\"\">")
               .format("<br><input type=\"submit\" value=\"Submit\"></form></body></html>");

            getResponse().setEntity(fmt.toString(), MediaType.TEXT_HTML);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
    }
    /**
     * Template method for determining which status to return on a successful ticket creation.
     * This method exists for compatibility reasons with bad clients (i.e. Flash) that can't
     * process 201 with a Location header.
     *
     * @return the status to return.
     */
    protected Status determineStatus() {
        return Status.SUCCESS_CREATED;
    }

    /**
     * Obtain credentials from the request.
     *
     * @return the credential
     */
    protected Credential obtainCredentials() {
        final UsernamePasswordCredential c = new UsernamePasswordCredential();
        final WebRequestDataBinder binder = new WebRequestDataBinder(c);
        final RestletWebRequest webRequest = new RestletWebRequest(getRequest());

        logFormRequest(new Form(getRequest().getEntity()));
        binder.bind(webRequest);

        return c;
    }

    /**
     * Log the form request.
     *
     * @param form the form
     */
    private void logFormRequest(final Form form) {
        if (LOGGER.isDebugEnabled()) {
            final Set<String> pairs = new HashSet<>();
            for (final String name : form.getNames()) {
                final StringBuilder builder = new StringBuilder();
                builder.append(name);
                builder.append(": ");
                if (!"password".equalsIgnoreCase(name)) {
                    builder.append(form.getValues(name));
                } else {
                    builder.append("*****");
                }
                pairs.add(builder.toString());
            }
            LOGGER.debug(StringUtils.join(pairs, ", "));
        }
    }

    protected static class RestletWebRequest implements WebRequest {
        private final Form form;
        private final Request request;

        /**
         * Instantiates a new restlet web request.
         *
         * @param request the request
         */
        public RestletWebRequest(final Request request) {
            this.form = new Form(request.getEntity());
            this.request = request;
        }

        @Override
        public boolean checkNotModified(final String s) {
            return false;
        }

        @Override
        public boolean checkNotModified(final long lastModifiedTimestamp) {
            return false;
        }

        @Override
        public String getContextPath() {
            return this.request.getResourceRef().getPath();
        }

        @Override
        public String getDescription(final boolean includeClientInfo) {
            return null;
        }

        @Override
        public Locale getLocale() {
            return LocaleContextHolder.getLocale();
        }

        @Override
        public String getParameter(final String paramName) {
            return this.form.getFirstValue(paramName);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            final Map<String, String[]> conversion = new HashMap<>();

            for (final Map.Entry<String, String> entry : this.form.getValuesMap().entrySet()) {
                conversion.put(entry.getKey(), new String[] {entry.getValue()});
            }

            return conversion;
        }

        @Override
        public String[] getParameterValues(final String paramName) {
            return this.form.getValuesArray(paramName);
        }

        @Override
        public String getRemoteUser() {
            return null;
        }

        @Override
        public Principal getUserPrincipal() {
            return null;
        }

        @Override
        public boolean isSecure() {
            return this.request.isConfidential();
        }

        @Override
        public boolean isUserInRole(final String role) {
            return false;
        }

        @Override
        public Object getAttribute(final String name, final int scope) {
            return null;
        }

        @Override
        public String[] getAttributeNames(final int scope) {
            return null;
        }

        @Override
        public String getSessionId() {
            return null;
        }

        @Override
        public Object getSessionMutex() {
            return null;
        }

        @Override
        public void registerDestructionCallback(final String name, final Runnable callback, final int scope) {
            // nothing to do
        }

        @Override
        public void removeAttribute(final String name, final int scope) {
            // nothing to do
        }

        @Override
        public void setAttribute(final String name, final Object value, final int scope) {
            // nothing to do
        }

        @Override
        public String getHeader(final String s) {
            return null;
        }

        @Override
        public String[] getHeaderValues(final String s) {
            return new String[0];
        }

        @Override
        public Iterator<String> getHeaderNames() {
            return null;
        }

        @Override
        public Iterator<String> getParameterNames() {
            return null;
        }

        @Override
        public Object resolveReference(final String s) {
            return null;
        }
    }
}
