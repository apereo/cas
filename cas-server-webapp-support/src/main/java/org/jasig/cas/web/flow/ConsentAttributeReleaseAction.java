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

package org.jasig.cas.web.flow;

import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.PrincipalException;
import org.jasig.cas.authentication.UnresolvedPrincipalException;
import org.jasig.cas.authentication.principal.NullPrincipal;
import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.AttributeReleaseConsentStrategy;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;

/**
 * An action to decide whether user-consent to attribute release
 * is in fact required. The outcome of this action is either
 * {@value #EVENT_AUTHORIZED} or {@value #EVENT_REQUIRED}.
 * @author Misagh Moayyed
 * @since 4.2
 */
public class ConsentAttributeReleaseAction extends AbstractAction {
    private static final String EVENT_REQUIRED = "required";
    private static final String EVENT_AUTHORIZED = "authorized";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ServicesManager servicesManager;
    private final CentralAuthenticationService centralAuthenticationService;
    private final AttributeReleaseConsentStrategy attributeReleaseConsentStrategy;

    private boolean alwaysRequireConsent;

    /**
     * Instantiates a new attribute release action.
     *
     * @param attributeReleaseConsentStrategy the attribute release consent strategy
     * @param centralAuthenticationService the central authentication service
     * @param servicesManager the services manager
     */
    public ConsentAttributeReleaseAction(
            final AttributeReleaseConsentStrategy attributeReleaseConsentStrategy,
            final CentralAuthenticationService centralAuthenticationService,
            final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
        this.centralAuthenticationService = centralAuthenticationService;
        this.attributeReleaseConsentStrategy = attributeReleaseConsentStrategy;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {

        final Service service = WebUtils.getService(requestContext);
        if (service == null) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                    "Service cannot be found in the request context");
        }
        final RegisteredService registeredService = getRegisteredService(service);
        final Principal principal = getAuthenticationPrincipal(requestContext);

        if (this.alwaysRequireConsent) {
            prepareModelData(requestContext, service, registeredService, principal);
            return new EventFactorySupport().event(this, EVENT_REQUIRED);
        }

        if (registeredService.getAttributeReleasePolicy().isAttributeConsentRequired()) {
            if (!isConsentRequired(registeredService, principal)) {
                prepareModelData(requestContext, service, registeredService, principal);
                return new EventFactorySupport().event(this, EVENT_REQUIRED);
            }
        }
        return new EventFactorySupport().event(this, EVENT_AUTHORIZED);
    }

    /**
     * Prepare model data by passing service, registered service
     * and principal to the flow.
     *
     * @param requestContext the request context
     * @param service the service
     * @param registeredService the registered service
     * @param principal the principal
     */
    private void prepareModelData(final RequestContext requestContext, final Service service,
                                  final RegisteredService registeredService, final Principal principal) {
        WebUtils.putService(requestContext, service);
        final Map<String, Object> attributes = registeredService.getAttributeReleasePolicy().getAttributes(principal);
        WebUtils.putIntoFlowScope(requestContext, "attributes", attributes);
    }

    /**
     * Is consent required for this service by this principal?
     *
     * @param registeredService the registered service
     * @param principal the principal
     * @return the boolean
     */
    protected boolean isConsentRequired(final RegisteredService registeredService, final Principal principal) {
        return this.attributeReleaseConsentStrategy.isAttributeReleaseConsented(registeredService, principal);
    }

    /**
     * Gets registered service from the context.
     *
     * @param service service
     * @return the registered service
     */
    protected RegisteredService getRegisteredService(final Service service) {
        final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
        if (registeredService == null || !registeredService.getAccessStrategy().isServiceAccessAllowed()) {
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE,
                    "Service cannot be found in the registry or access is not authorized");
        }
        return registeredService;
    }

    public void setAlwaysRequireConsent(final boolean alwaysRequireConsent) {
        this.alwaysRequireConsent = alwaysRequireConsent;
    }

    /**
     * Gets authentication principal.
     *
     * @param requestContext the request context
     * @return the authentication principal
     */
    protected Principal getAuthenticationPrincipal(final RequestContext requestContext) {
        try {
            final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(requestContext);
            final TicketGrantingTicket ticketGrantingTicket =
                    this.centralAuthenticationService.getTicket(ticketGrantingTicketId,
                            TicketGrantingTicket.class);
            return ticketGrantingTicket.getAuthentication().getPrincipal();
        } catch (final InvalidTicketException e){
            logger.warn(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

