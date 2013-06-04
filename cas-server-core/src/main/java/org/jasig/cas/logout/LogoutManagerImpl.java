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
package org.jasig.cas.logout;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;

import javax.validation.constraints.NotNull;

import org.apache.commons.codec.binary.Base64;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SingleLogoutService;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.util.HttpClient;
import org.jasig.cas.util.SamlDateUtils;
import org.jasig.cas.util.UniqueTicketIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This logout manager handles the Single Log Out process.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public final class LogoutManagerImpl implements LogoutManager {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutManagerImpl.class);

    /** The logout request template. */
    private static final String LOGOUT_REQUEST_TEMPLATE =
            "<samlp:LogoutRequest xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"%s\" Version=\"2.0\""
            + "IssueInstant=\"%s\"><saml:NameID xmlns:saml=\"urn:oasis:names:tc:SAML:2.0:assertion\">@NOT_USED@"
            + "</saml:NameID><samlp:SessionIndex>%s</samlp:SessionIndex></samlp:LogoutRequest>";

    /** ASCII character set. */
    private static final Charset ASCII = Charset.forName("ASCII");

    /** A ticket Id generator. */
    private static final UniqueTicketIdGenerator GENERATOR = new DefaultUniqueTicketIdGenerator();

    /** The services manager. */
    @NotNull
    private final ServicesManager servicesManager;

    /** An HTTP client. */
    @NotNull
    private final HttpClient httpClient;

    /** Whether single sign out is disabled or not. */
    private boolean disableSingleSignOut = false;

    /**
     * Build the logout manager.
     * @param servicesManager the services manager.
     * @param httpClient an HTTP client.
     */
    public LogoutManagerImpl(final ServicesManager servicesManager, final HttpClient httpClient) {
        this.servicesManager = servicesManager;
        this.httpClient = httpClient;
    }

    /**
     * Perform a back channel logout for a given ticket granting ticket and returns all the logout requests.
     *
     * @param ticket a given ticket granting ticket.
     * @return all logout requests.
     */
    @Override
    public List<LogoutRequest> performLogout(final TicketGrantingTicket ticket) {
        final Map<String, Service> services;
        // synchronize the retrieval of the services and their cleaning for the TGT
        // to avoid concurrent logout mess ups
        synchronized (ticket) {
            services = ticket.getServices();
            ticket.removeAllServices();
        }
        ticket.markTicketExpired();

        final List<LogoutRequest> logoutRequests = new ArrayList<LogoutRequest>();
        // if SLO is not disabled
        if (!disableSingleSignOut) {
            // through all services
            for (final String ticketId : services.keySet()) {
                final Service service = services.get(ticketId);
                // it's a SingleLogoutService, else ignore
                if (service instanceof SingleLogoutService) {
                    final SingleLogoutService singleLogoutService = (SingleLogoutService) service;
                    // the logout has not performed already
                    if (!singleLogoutService.isLoggedOutAlready()) {
                        final LogoutRequest logoutRequest = new LogoutRequest(ticketId, service);
                        // always add the logout request
                        logoutRequests.add(logoutRequest);
                        final RegisteredService registeredService = servicesManager.findServiceBy(service);
                        // the service is no more defined, or the logout type is not defined or is back channel
                        if (registeredService == null || registeredService.getLogoutType() == null
                                || registeredService.getLogoutType() == LogoutType.BACK_CHANNEL) {
                            // perform back channel logout
                            if (performBackChannelLogout(singleLogoutService, ticketId)) {
                                logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
                            } else {
                                logoutRequest.setStatus(LogoutRequestStatus.FAILURE);
                                LOGGER.warn("Logout message not sent to [[]]; Continuing processing...",
                                        singleLogoutService.getId());
                            }
                        }
                    }
                }
            }
        }

        return logoutRequests;
    }

    /**
     * Log out of a service through back channel.
     *
     * @param service the service to log out.
     * @param ticketId the ticket id.
     * @return if the logout has been performed.
     */
    private boolean performBackChannelLogout(final SingleLogoutService service, final String ticketId) {
        LOGGER.debug("Sending logout request for: {}", service.getId());

        final String logoutRequest = createBackChannelLogoutMessage(ticketId);

        service.setLoggedOutAlready(true);

        return this.httpClient.sendMessageToEndPoint(service.getOriginalUrl(), logoutRequest, true);
    }

    /**
     * Create a logout message for front channel logout.
     *
     * @param logoutRequest the logout request.
     * @return a front SAML logout message.
     */
    public String createFrontChannelLogoutMessage(final LogoutRequest logoutRequest) {
        final String logoutMessage = createBackChannelLogoutMessage(logoutRequest.getTicketId());
        final Deflater deflater = new Deflater();
        deflater.setInput(logoutMessage.getBytes(ASCII));
        deflater.finish();
        final byte[] buffer = new byte[logoutMessage.length()];
        final int resultSize = deflater.deflate(buffer);
        final byte[] output = new byte[resultSize];
        System.arraycopy(buffer, 0, output, 0, resultSize);
        return Base64.encodeBase64String(output);
    }

    /**
     * Create a logout message for back channel logout.
     *
     * @param ticketId the ticket id.
     * @return a back channel logout.
     */
    private String createBackChannelLogoutMessage(final String ticketId) {
        final String logoutRequest = String.format(LOGOUT_REQUEST_TEMPLATE, GENERATOR.getNewTicketId("LR"),
                SamlDateUtils.getCurrentDateAndTime(), ticketId);
        return logoutRequest;
    }

    /**
     * Set if the logout is disabled.
     *
     * @param disableSingleSignOut if the logout is disabled.
     */
    public void setDisableSingleSignOut(final boolean disableSingleSignOut) {
        this.disableSingleSignOut = disableSingleSignOut;
    }
}
