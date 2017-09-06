package org.apereo.cas.logout;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.WebApplicationService;
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

    private final ServicesManager servicesManager;
    private final HttpClient httpClient;
    private boolean asynchronous = true;
    private final LogoutMessageCreator logoutMessageBuilder;
    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;
    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    /**
     * Instantiates a new Single logout service message handler.
     *
     * @param httpClient                                      to send the requests
     * @param logoutMessageCreator                            creates the message
     * @param servicesManager                                 finds services to logout from
     * @param singleLogoutServiceLogoutUrlBuilder             creates the URL
     * @param asyncCallbacks                                  if messages are sent in an asynchronous fashion.
     * @param authenticationRequestServiceSelectionStrategies the authentication request service selection strategies
     */
    public DefaultSingleLogoutServiceMessageHandler(final HttpClient httpClient, final LogoutMessageCreator logoutMessageCreator,
                                                    final ServicesManager servicesManager,
                                                    final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder,
                                                    final boolean asyncCallbacks,
                                                    final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies) {
        this.httpClient = httpClient;
        this.logoutMessageBuilder = logoutMessageCreator;
        this.servicesManager = servicesManager;
        this.singleLogoutServiceLogoutUrlBuilder = singleLogoutServiceLogoutUrlBuilder;
        this.asynchronous = asyncCallbacks;
        this.authenticationRequestServiceSelectionStrategies = authenticationRequestServiceSelectionStrategies;
    }

    /**
     * Handle logout for slo service.
     *
     * @param singleLogoutService the service
     * @param ticketId            the ticket id
     * @return the logout request
     */
    @Override
    public LogoutRequest handle(final WebApplicationService singleLogoutService, final String ticketId) {
        if (singleLogoutService.isLoggedOutAlready()) {
            LOGGER.debug("Service [{}] is already logged out.", singleLogoutService);
            return null;
        }

        final WebApplicationService selectedService = WebApplicationService.class.cast(
                this.authenticationRequestServiceSelectionStrategies.resolveService(singleLogoutService));

        LOGGER.debug("Processing logout request for service [{}]...", selectedService);
        final RegisteredService registeredService = this.servicesManager.findServiceBy(selectedService);

        if (!serviceSupportsSingleLogout(registeredService)) {
            LOGGER.debug("Service [{}] does not support single logout.", selectedService);
            return null;
        }
        LOGGER.debug("Service [{}] supports single logout and is found in the registry as [{}]. Proceeding...", selectedService, registeredService);

        final URL logoutUrl = this.singleLogoutServiceLogoutUrlBuilder.determineLogoutUrl(registeredService, selectedService);
        LOGGER.debug("Prepared logout url [{}] for service [{}]", logoutUrl, selectedService);
        if (logoutUrl == null) {
            LOGGER.debug("Service [{}] does not support logout operations given no logout url could be determined.", selectedService);
            return null;
        }

        LOGGER.debug("Creating logout request for [{}] and ticket id [{}]", selectedService, ticketId);
        final DefaultLogoutRequest logoutRequest = new DefaultLogoutRequest(ticketId, selectedService, logoutUrl);
        LOGGER.debug("Logout request [{}] created for [{}] and ticket id [{}]", logoutRequest, selectedService, ticketId);

        final RegisteredService.LogoutType type = registeredService.getLogoutType() == null
                ? RegisteredService.LogoutType.BACK_CHANNEL : registeredService.getLogoutType();
        LOGGER.debug("Logout type registered for [{}] is [{}]", selectedService, type);

        switch (type) {
            case BACK_CHANNEL:
                if (performBackChannelLogout(logoutRequest)) {
                    logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
                } else {
                    logoutRequest.setStatus(LogoutRequestStatus.FAILURE);
                    LOGGER.warn("Logout message is not sent to [{}]; Continuing processing...", singleLogoutService.getId());
                }
                break;
            default:
                LOGGER.debug("Logout operation is not yet attempted for [{}] given logout type is set to [{}]", selectedService, type);
                logoutRequest.setStatus(LogoutRequestStatus.NOT_ATTEMPTED);
                break;
        }
        return logoutRequest;

    }

    /**
     * Log out of a service through back channel.
     *
     * @param request the logout request.
     * @return if the logout has been performed.
     */
    public boolean performBackChannelLogout(final LogoutRequest request) {
        try {
            LOGGER.debug("Creating back-channel logout request based on [{}]", request);
            final String logoutRequest = this.logoutMessageBuilder.create(request);
            final WebApplicationService logoutService = request.getService();
            logoutService.setLoggedOutAlready(true);

            LOGGER.debug("Preparing logout request for [{}] to [{}]", logoutService.getId(), request.getLogoutUrl());
            final LogoutHttpMessage msg = new LogoutHttpMessage(request.getLogoutUrl(), logoutRequest, this.asynchronous);
            LOGGER.debug("Prepared logout message to send is [{}]. Sending...", msg);
            return this.httpClient.sendMessageToEndPoint(msg);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Service supports back channel single logout?
     * Service must be found in the registry. enabled and logout type must not be {@link RegisteredService.LogoutType#NONE}.
     *
     * @param registeredService the registered service
     * @return true, if support is available.
     */
    private static boolean serviceSupportsSingleLogout(final RegisteredService registeredService) {
        return registeredService != null
                && registeredService.getAccessStrategy().isServiceAccessAllowed()
                && registeredService.getLogoutType() != RegisteredService.LogoutType.NONE;
    }

    public ServicesManager getServicesManager() {
        return this.servicesManager;
    }
}
