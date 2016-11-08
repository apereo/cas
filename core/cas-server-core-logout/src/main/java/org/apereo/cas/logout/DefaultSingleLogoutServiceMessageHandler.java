package org.apereo.cas.logout;

import org.apereo.cas.services.LogoutType;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/**
 * This is {@link DefaultSingleLogoutServiceMessageHandler} which handles the processing of logout messages
 * to logout endpoints processed by the logout manager.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultSingleLogoutServiceMessageHandler implements SingleLogoutServiceMessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSingleLogoutServiceMessageHandler.class);
    
    private ServicesManager servicesManager;
    
    private HttpClient httpClient;
    
    private boolean asynchronous = true;
    
    private LogoutMessageCreator logoutMessageBuilder = new SamlCompliantLogoutMessageCreator();
    
    private SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder = 
            new DefaultSingleLogoutServiceLogoutUrlBuilder();

    /**
     * Instantiates a new Single logout service message handler.
     */
    public DefaultSingleLogoutServiceMessageHandler() {}

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
     * Handle logout for slo service.
     *
     * @param singleLogoutService the service
     * @param ticketId the ticket id
     * @return the logout request
     */
    @Override
    public LogoutRequest handle(final SingleLogoutService singleLogoutService, final String ticketId) {
        if (!singleLogoutService.isLoggedOutAlready()) {

            final RegisteredService registeredService = this.servicesManager.findServiceBy(singleLogoutService);
            if (serviceSupportsSingleLogout(registeredService)) {
                final URL logoutUrl = this.singleLogoutServiceLogoutUrlBuilder.determineLogoutUrl(registeredService, singleLogoutService);
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
     * Log out of a service through back channel.
     *
     * @param request the logout request.
     * @return if the logout has been performed.
     */
    public boolean performBackChannelLogout(final LogoutRequest request) {
        try {
            final String logoutRequest = this.logoutMessageBuilder.create(request);
            final SingleLogoutService logoutService = request.getService();
            logoutService.setLoggedOutAlready(true);

            LOGGER.debug("Sending logout request for [{}] to [{}]", logoutService.getId(), request.getLogoutUrl());
            final LogoutHttpMessage msg = new LogoutHttpMessage(request.getLogoutUrl(), logoutRequest, this.asynchronous);
            LOGGER.debug("Prepared logout message to send is [{}]", msg);
            return this.httpClient.sendMessageToEndPoint(msg);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Service supports back channel single logout?
     * Service must be found in the registry. enabled and logout type must not be {@link LogoutType#NONE}.
     * @param registeredService the registered service
     * @return true, if support is available.
     */
    private static boolean serviceSupportsSingleLogout(final RegisteredService registeredService) {
        return registeredService != null
                && registeredService.getAccessStrategy().isServiceAccessAllowed()
                && registeredService.getLogoutType() != LogoutType.NONE;
    }

    public void setServicesManager(final ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    public void setHttpClient(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void setLogoutMessageBuilder(final LogoutMessageCreator logoutMessageBuilder) {
        this.logoutMessageBuilder = logoutMessageBuilder;
    }

    public void setSingleLogoutServiceLogoutUrlBuilder(final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder) {
        this.singleLogoutServiceLogoutUrlBuilder = singleLogoutServiceLogoutUrlBuilder;
    }

    public ServicesManager getServicesManager() {
        return this.servicesManager;
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    public boolean isAsynchronous() {
        return this.asynchronous;
    }

    public SingleLogoutServiceLogoutUrlBuilder getSingleLogoutServiceLogoutUrlBuilder() {
        return this.singleLogoutServiceLogoutUrlBuilder;
    }

    public LogoutMessageCreator getLogoutMessageBuilder() {
        return this.logoutMessageBuilder;
    }
}
