/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.integration.restlet;

import java.security.Principal;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.TicketException;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.bind.support.WebRequestDataBinder;
import org.springframework.web.context.request.WebRequest;

/**
 * Handles the creation of Ticket Granting Tickets.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.3
 * 
 */
public class TicketResource extends Resource {
    
    private static final Log log = LogFactory.getLog(TicketResource.class);

    @Autowired
    private CentralAuthenticationService centralAuthenticationService;
    
    public final boolean allowGet() {
        return false;
    }

    public final boolean allowPost() {
        return true;
    }

    public final void acceptRepresentation(final Representation entity)
        throws ResourceException {
        if (log.isDebugEnabled()) {
            log.debug("Obtaining credentials...");
            log.debug(getRequest().getEntityAsForm().toString());
        }
     
        final Credentials c = obtainCredentials();
        try {
            final String ticketGrantingTicketId = this.centralAuthenticationService.createTicketGrantingTicket(c);
            getResponse().setStatus(Status.SUCCESS_CREATED);
            final Reference ticket_ref = getRequest().getResourceRef().addSegment(ticketGrantingTicketId);
            getResponse().setLocationRef(ticket_ref); 
        } catch (final TicketException e) {
            log.error(e,e);
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
        }
    }
    
    protected Credentials obtainCredentials() {
        final UsernamePasswordCredentials c = new UsernamePasswordCredentials();
        final WebRequestDataBinder binder = new WebRequestDataBinder(c);
        final RestletWebRequest webRequest = new RestletWebRequest(getRequest());
        
        if (log.isDebugEnabled()) {
            log.debug(getRequest().getEntityAsForm().toString());
            log.debug("Username from RestletWebRequest: " + webRequest.getParameter("username"));
        }
        
        binder.bind(webRequest);
        
        return c;
    }
    
    protected class RestletWebRequest implements WebRequest {
        
        private final Form form;
        
        private final Request request;
        
        public RestletWebRequest(final Request request) {
            this.form = getRequest().getEntityAsForm();
            this.request = request;
        }

        public boolean checkNotModified(long lastModifiedTimestamp) {
            return false;
        }

        public String getContextPath() {
            return this.request.getResourceRef().getPath();
        }

        public String getDescription(boolean includeClientInfo) {
            return null;
        }

        public Locale getLocale() {
            return LocaleContextHolder.getLocale();
        }

        public String getParameter(String paramName) {
            return this.form.getFirstValue(paramName);
        }

        public Map getParameterMap() {
            return this.form.getValuesMap();
        }

        public String[] getParameterValues(String paramName) {
            return this.form.getValuesArray(paramName);
        }

        public String getRemoteUser() {
            return null;
        }

        public Principal getUserPrincipal() {
            return null;
        }

        public boolean isSecure() {
            return this.request.isConfidential();
        }

        public boolean isUserInRole(String role) {
            return false;
        }

        public Object getAttribute(String name, int scope) {
            return null;
        }

        public String[] getAttributeNames(int scope) {
            return null;
        }

        public String getSessionId() {
            return null;
        }

        public Object getSessionMutex() {
            return null;
        }

        public void registerDestructionCallback(String name, Runnable callback,
            int scope) {
            // nothing to do
        }

        public void removeAttribute(String name, int scope) {
            // nothing to do
        }

        public void setAttribute(String name, Object value, int scope) {
            // nothing to do
        }
    }
}
