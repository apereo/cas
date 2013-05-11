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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
import org.jasig.cas.util.Pair;
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
    private final Logger log = LoggerFactory.getLogger(LogoutManagerImpl.class);

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
     * Perform a back channel logout for a given ticket granting ticket and returns the services
     * eligible to a front channel logout.
     *
     * @param ticket a given ticket granting ticket.
     * @return an interator on front channel logout services
     */
    @Override
    public Iterator<Pair<String, Service>> performLogout(final TicketGrantingTicket ticket) {
        final Collection<Pair<String, Service>> services;
        // synchronize the retrieval of the services and their cleaning for the TGT
        // to avoid concurrent logout mess ups
        synchronized (ticket) {
            services = ticket.getServices();
            ticket.removeAllServices();
        }
        ticket.markTicketExpired();

        final List<Pair<String, Service>> frontServices = new ArrayList<Pair<String, Service>>();
        // if SLO is not disabled
        if (!disableSingleSignOut) {
            // through all services
            for (final Pair<String, Service> ticketedService : services) {
                final String ticketId = ticketedService.getFirst();
                final Service service = ticketedService.getSecond();
                // it's a SingleLogoutService, else ignore
                if (service instanceof SingleLogoutService) {
                    final SingleLogoutService singleLogoutService = (SingleLogoutService) service;
                    // the logout has not performed already
                    if (!singleLogoutService.isLoggedOutAlready()) {
                        final RegisteredService registeredService = servicesManager.findServiceBy(service);
                        // it's a front channel logout service
                        if (registeredService != null
                                && registeredService.getLogoutType() == LogoutType.FRONT_CHANNEL) {
                            // keep it for later front logout
                            frontServices.add(ticketedService);
                        } else {
                            // perform back channel logout
                            if (!performBackChannelLogout(singleLogoutService, ticketId)) {
                                log.warn("Logout message not sent to [[]]; Continuing processing...",
                                        singleLogoutService.getId());
                            }
                        }
                    }
                }
            }
        }

        return frontServices.iterator();
    }

    /**
     * Log out of a service through back channel.
     *
     * @param service the service to log out.
     * @param ticketId the ticket id.
     * @return if the logout has been performed.
     */
    private boolean performBackChannelLogout(final SingleLogoutService service, final String ticketId) {
        log.debug("Sending logout request for: {}", service.getId());

        final String logoutRequest = createBackChannelLogoutMessage(ticketId);

        service.setLoggedOutAlready(true);

        return this.httpClient.sendMessageToEndPoint(service.getOriginalUrl(), logoutRequest, true);
    }

    /**
     * Create a logout message for front channel logout.
     *
     * @param ticketId the ticket id.
     * @return a front logout message.
     */
    private String createFrontChannelLogoutMessage(final String ticketId) {
        final String logoutRequest = createBackChannelLogoutMessage(ticketId);
        final Deflater deflater = new Deflater();
        deflater.setInput(logoutRequest.getBytes(ASCII));
        deflater.finish();
        final byte[] buffer = new byte[logoutRequest.length()];
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
        final String logoutRequest =
                            String.format(LOGOUT_REQUEST_TEMPLATE, GENERATOR.getNewTicketId("LR"),
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
