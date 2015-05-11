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

import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.CentralAuthenticationService;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.validation.constraints.NotNull;

/**
 * Action that handles the TicketGrantingTicket creation and destruction. If the
 * action is given a TicketGrantingTicket and one also already exists, the old
 * one is destroyed and replaced with the new one. This action always returns
 * "success".
 *
 * @author Scott Battaglia
 * @since 3.0.0
 */
public final class SendTicketGrantingTicketAction extends AbstractAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendTicketGrantingTicketAction.class);

    private boolean createSsoSessionCookieOnRenewAuthentications = true;

    @NotNull
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    /** Instance of CentralAuthenticationService. */
    @NotNull
    private final CentralAuthenticationService centralAuthenticationService;

    @NotNull
    private final ServicesManager servicesManager;

    /**
     * Instantiates a new Send ticket granting ticket action.
     *
     * @param ticketGrantingTicketCookieGenerator the ticket granting ticket cookie generator
     * @param centralAuthenticationService the central authentication service
     * @param servicesManager the services manager
     */
    public SendTicketGrantingTicketAction(final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator,
                                          final CentralAuthenticationService centralAuthenticationService,
                                          final ServicesManager servicesManager) {
        super();
        this.ticketGrantingTicketCookieGenerator = ticketGrantingTicketCookieGenerator;
        this.centralAuthenticationService = centralAuthenticationService;
        this.servicesManager = servicesManager;
    }

    @Override
    protected Event doExecute(final RequestContext context) {
        final String ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(context);
        final String ticketGrantingTicketValueFromCookie = (String) context.getFlowScope().get("ticketGrantingTicketId");

        if (ticketGrantingTicketId == null) {
            return success();
        }

        if (isAuthenticatingAtPublicWorkstation(context))  {
            LOGGER.info("Authentication is at a public workstation. "
                    + "SSO cookie will not be generated. Subsequent requests will be challenged for authentication.");
        } else if (!this.createSsoSessionCookieOnRenewAuthentications && isAuthenticationRenewed(context)) {
            LOGGER.info("Authentication session is renewed but CAS is not configured to create the SSO session. "
                    + "SSO cookie will not be generated. Subsequent requests will be challenged for authentication.");
        } else {
            this.ticketGrantingTicketCookieGenerator.addCookie(WebUtils.getHttpServletRequest(context), WebUtils
                .getHttpServletResponse(context), ticketGrantingTicketId);
        }

        if (ticketGrantingTicketValueFromCookie != null && !ticketGrantingTicketId.equals(ticketGrantingTicketValueFromCookie)) {
            this.centralAuthenticationService
                .destroyTicketGrantingTicket(ticketGrantingTicketValueFromCookie);
        }

        return success();
    }

    /**
     * @deprecated As of 4.1, use constructors instead.
     * @param ticketGrantingTicketCookieGenerator the id generator
     */
    @Deprecated
    public void setTicketGrantingTicketCookieGenerator(final CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator) {
        logger.warn("setTicketGrantingTicketCookieGenerator() is deprecated and has no effect. Use constructors instead.");
    }

    /**
     * @deprecated As of 4.1, use constructors instead.
     * @param centralAuthenticationService  cas instance
     */
    @Deprecated
    public void setCentralAuthenticationService(
        final CentralAuthenticationService centralAuthenticationService) {
        logger.warn("setCentralAuthenticationService() is deprecated and has no effect. Use constructors instead.");
    }

    public void setCreateSsoSessionCookieOnRenewAuthentications(final boolean createSsoSessionCookieOnRenewAuthentications) {
        this.createSsoSessionCookieOnRenewAuthentications = createSsoSessionCookieOnRenewAuthentications;
    }

    /**
     * @deprecated As of 4.1, use constructors instead.
     * @param servicesManager  the service manager
     */
    @Deprecated
    public void setServicesManager(final ServicesManager servicesManager) {
        logger.warn("setServicesManager() is deprecated and has no effect. Use constructors instead.");
    }

    /**
     * Tries to determine if authentication was created as part of a "renew" event.
     * Renewed authentications can occur if the service is not allowed to participate
     * in SSO or if a "renew" request parameter is specified.
     *
     * @param ctx the request context
     * @return true if renewed
     */
    private boolean isAuthenticationRenewed(final RequestContext ctx) {
        if (ctx.getRequestParameters().contains(CasProtocolConstants.PARAMETER_RENEW)) {
            LOGGER.debug("[{}] is specified for the request. The authentication session will be considered renewed.",
                    CasProtocolConstants.PARAMETER_RENEW);
            return true;
        }

        final Service service = WebUtils.getService(ctx);
        if (service != null) {
            final RegisteredService registeredService = this.servicesManager.findServiceBy(service);
            if (registeredService != null) {
                final boolean isAllowedForSso = registeredService.getAccessStrategy().isServiceAccessAllowedForSso();
                LOGGER.debug("Located [{}] in registry. Service access to participate in SSO is set to [{}]",
                        registeredService.getServiceId(), isAllowedForSso);
                return !isAllowedForSso;
            }
        }

        return false;
    }

    /**
     * Is authenticating at a public workstation?
     *
     * @param ctx the ctx
     * @return true if the cookie value is present
     */
    private boolean isAuthenticatingAtPublicWorkstation(final RequestContext ctx) {
        if (ctx.getFlowScope().contains(AuthenticationViaFormAction.PUBLIC_WORKSTATION_ATTRIBUTE)) {
            LOGGER.debug("Public workstation flag detected. SSO session will be considered renewed.");
            return true;
        }
        return false;
    }
}
