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
package org.jasig.cas.logout;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.authentication.principal.SingleLogoutService;
import org.jasig.cas.services.LogoutType;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.util.CompressionUtils;
import org.jasig.cas.util.http.HttpClient;
import org.jasig.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import javax.validation.constraints.NotNull;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This logout manager handles the Single Log Out process.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public final class LogoutManagerImpl implements LogoutManager {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutManagerImpl.class);

    /** The parameter name that contains the logout request. */
    private static final String LOGOUT_PARAMETER_NAME = "logoutRequest";

    /** ASCII character set. */
    private static final Charset ASCII = Charset.forName("ASCII");

    /** The services manager. */
    @NotNull
    private final ServicesManager servicesManager;

    /** An HTTP client. */
    @NotNull
    private final HttpClient httpClient;

    @NotNull
    private final LogoutMessageCreator logoutMessageBuilder;
    
    /** Whether single sign out is disabled or not. */
    private boolean singleLogoutCallbacksDisabled;
    
    /** 
     * Whether messages to endpoints would be sent in an asynchronous fashion.
     * True by default.
     **/
    private boolean asynchronous = true;
    
    /**
     * Build the logout manager.
     * @param servicesManager the services manager.
     * @param httpClient an HTTP client.
     * @param logoutMessageBuilder the builder to construct logout messages.
     */
    public LogoutManagerImpl(final ServicesManager servicesManager, final HttpClient httpClient,
                             final LogoutMessageCreator logoutMessageBuilder) {
        this.servicesManager = servicesManager;
        this.httpClient = httpClient;
        this.logoutMessageBuilder = logoutMessageBuilder;
    }

    /**
     * Set if messages are sent in an asynchronous fashion.
     *
     * @param asyncCallbacks if message is synchronously sent
     * @since 4.1.0
     */
    public void setAsynchronous(final boolean asyncCallbacks) {
        this.asynchronous = asyncCallbacks;
    }
    
    /**
     * Set if messages are sent in an asynchronous fashion.
     *
     * @param asyncCallbacks if message is synchronously sent
     * @deprecated As of 4.1. Use {@link #setAsynchronous(boolean)} instead
     */
    @Deprecated
    public void setIssueAsynchronousCallbacks(final boolean asyncCallbacks) {
        this.asynchronous = asyncCallbacks;
        LOGGER.warn("setIssueAsynchronousCallbacks() is deprecated. Use setAsynchronous() instead.");
    }
    
    /**
     * Perform a back channel logout for a given ticket granting ticket and returns all the logout requests.
     *
     * @param ticket a given ticket granting ticket.
     * @return all logout requests.
     */
    @Override
    public List<LogoutRequest> performLogout(final TicketGrantingTicket ticket) {
        final Map<String, Service> services = ticket.getServices();
        final List<LogoutRequest> logoutRequests = new ArrayList<>();
        // if SLO is not disabled
        if (!this.singleLogoutCallbacksDisabled) {
            // through all services
            for (final Map.Entry<String, Service> entry : services.entrySet()) {
                // it's a SingleLogoutService, else ignore
                final Service service = entry.getValue();
                if (service instanceof SingleLogoutService) {
                    final LogoutRequest logoutRequest = handleLogoutForSloService((SingleLogoutService) service, entry.getKey());
                    if (logoutRequest != null) {
                        LOGGER.debug("Captured logout request [{}]", logoutRequest);
                        logoutRequests.add(logoutRequest);
                    }
                }
            }
        }

        return logoutRequests;
    }

    /**
     * Service supports back channel single logout?
     * Service must be found in the registry. enabled and logout type must not be {@link LogoutType#NONE}.
     * @param registeredService the registered service
     * @return true, if support is available.
     */
    private boolean serviceSupportsSingleLogout(final RegisteredService registeredService) {
        return registeredService != null
                && registeredService.getAccessStrategy().isServiceAccessAllowed()
                && registeredService.getLogoutType() != LogoutType.NONE;
    }

    /**
     * Handle logout for slo service.
     *
     * @param singleLogoutService the service
     * @param ticketId the ticket id
     * @return the logout request
     */
    private LogoutRequest handleLogoutForSloService(final SingleLogoutService singleLogoutService, final String ticketId) {
        if (!singleLogoutService.isLoggedOutAlready()) {

            final RegisteredService registeredService = servicesManager.findServiceBy(singleLogoutService);
            if (serviceSupportsSingleLogout(registeredService)) {

                final URL logoutUrl = determineLogoutUrl(registeredService, singleLogoutService);
                final DefaultLogoutRequest logoutRequest = new DefaultLogoutRequest(ticketId, singleLogoutService, logoutUrl);
                final LogoutType type = registeredService.getLogoutType() == null
                        ? LogoutType.BACK_CHANNEL : registeredService.getLogoutType();

                switch (type) {
                    case BACK_CHANNEL:
                        if (performBackChannelLogout(logoutRequest)) {
                            logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
                        } else {
                            logoutRequest.setStatus(LogoutRequestStatus.FAILURE);
                            LOGGER.warn("Logout message not sent to [{}]; Continuing processing...", singleLogoutService.getId());
                        }
                        break;
                    default:
                        logoutRequest.setStatus(LogoutRequestStatus.NOT_ATTEMPTED);
                        break;
                }
                return logoutRequest;
            }

        }
        return null;
    }

    /**
     * Determine logout url.
     *
     * @param registeredService the registered service
     * @param singleLogoutService the single logout service
     * @return the uRL
     */
    private URL determineLogoutUrl(final RegisteredService registeredService, final SingleLogoutService singleLogoutService) {
        try {
            URL logoutUrl = new URL(singleLogoutService.getOriginalUrl());
            final URL serviceLogoutUrl = registeredService.getLogoutUrl();

            if (serviceLogoutUrl != null) {
                LOGGER.debug("Logout request will be sent to [{}] for service [{}]",
                        serviceLogoutUrl, singleLogoutService);
                logoutUrl = serviceLogoutUrl;
            }
            return logoutUrl;
        } catch (final Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Log out of a service through back channel.
     *
     * @param request the logout request.
     * @return if the logout has been performed.
     */
    private boolean performBackChannelLogout(final LogoutRequest request) {
        try {
            final String logoutRequest = this.logoutMessageBuilder.create(request);
            final SingleLogoutService logoutService = request.getService();
            logoutService.setLoggedOutAlready(true);
    
            LOGGER.debug("Sending logout request for: [{}]", logoutService.getId());
            final LogoutHttpMessage msg = new LogoutHttpMessage(request.getLogoutUrl(), logoutRequest);
            LOGGER.debug("Prepared logout message to send is [{}]", msg);
            return this.httpClient.sendMessageToEndPoint(msg);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Create a logout message for front channel logout.
     *
     * @param logoutRequest the logout request.
     * @return a front SAML logout message.
     */
    public String createFrontChannelLogoutMessage(final LogoutRequest logoutRequest) {
        final String logoutMessage = this.logoutMessageBuilder.create(logoutRequest);
        LOGGER.trace("Attempting to deflate the logout message [{}]", logoutMessage);
        return CompressionUtils.deflate(logoutMessage);
    }

    /**
     * Set if the logout is disabled.
     *
     * @param singleLogoutCallbacksDisabled if the logout is disabled.
     */
    public void setSingleLogoutCallbacksDisabled(final boolean singleLogoutCallbacksDisabled) {
        this.singleLogoutCallbacksDisabled = singleLogoutCallbacksDisabled;
    }
           
    /**
     * A logout http message that is accompanied by a special content type
     * and formatting.
     * @since 4.1.0
     */
    private final class LogoutHttpMessage extends HttpMessage {
        
        /**
         * Constructs a logout message, whose method of submission
         * is controlled by the {@link LogoutManagerImpl#asynchronous}.
         * 
         * @param url The url to send the message to
         * @param message Message to send to the url
         */
        LogoutHttpMessage(final URL url, final String message) {
            super(url, message, LogoutManagerImpl.this.asynchronous);
            setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        }

        /**
         * {@inheritDoc}.
         * Prepends the string "<code>logoutRequest=</code>" to the message body.
         */
        @Override
        protected String formatOutputMessageInternal(final String message) {
            return LOGOUT_PARAMETER_NAME + "=" + super.formatOutputMessageInternal(message);
        }        
    }
}
