package org.apereo.cas.logout;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.http.HttpClient;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultSingleLogoutServiceMessageHandler} which handles the processing of logout messages
 * to logout endpoints processed by the logout manager.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@AllArgsConstructor
public class DefaultSingleLogoutServiceMessageHandler implements SingleLogoutServiceMessageHandler {
    private final HttpClient httpClient;
    private final LogoutMessageCreator logoutMessageBuilder;
    private final ServicesManager servicesManager;
    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;
    private boolean asynchronous = true;
    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    /**
     * Handle logout for slo service.
     *
     * @param singleLogoutService the service
     * @param ticketId            the ticket id
     * @return the logout request
     */
    @Override
    public Collection<LogoutRequest> handle(final WebApplicationService singleLogoutService, final String ticketId) {
        if (singleLogoutService.isLoggedOutAlready()) {
            LOGGER.debug("Service [{}] is already logged out.", singleLogoutService);
            return new ArrayList<>(0);
        }

        final var selectedService = WebApplicationService.class.cast(
            this.authenticationRequestServiceSelectionStrategies.resolveService(singleLogoutService));

        LOGGER.debug("Processing logout request for service [{}]...", selectedService);
        final var registeredService = this.servicesManager.findServiceBy(selectedService);

        if (!serviceSupportsSingleLogout(registeredService)) {
            LOGGER.debug("Service [{}] does not support single logout.", selectedService);
            return new ArrayList<>(0);
        }
        LOGGER.debug("Service [{}] supports single logout and is found in the registry as [{}]. Proceeding...", selectedService, registeredService);

        final var logoutUrls = this.singleLogoutServiceLogoutUrlBuilder.determineLogoutUrl(registeredService, selectedService);
        LOGGER.debug("Prepared logout url [{}] for service [{}]", logoutUrls, selectedService);
        if (logoutUrls == null || logoutUrls.isEmpty()) {
            LOGGER.debug("Service [{}] does not support logout operations given no logout url could be determined.", selectedService);
            return new ArrayList<>(0);
        }

        LOGGER.debug("Creating logout request for [{}] and ticket id [{}]", selectedService, ticketId);
        return createLogoutRequests(ticketId, selectedService, registeredService, logoutUrls);
    }

    private Collection<LogoutRequest> createLogoutRequests(final String ticketId,
                                                           final WebApplicationService selectedService,
                                                           final RegisteredService registeredService,
                                                           final Collection<URL> logoutUrls) {
        return logoutUrls
            .stream()
            .map(url -> createLogoutRequest(ticketId, selectedService, registeredService, url))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private LogoutRequest createLogoutRequest(final String ticketId,
                                              final WebApplicationService selectedService,
                                              final RegisteredService registeredService,
                                              final URL logoutUrl) {
        final var logoutRequest = new DefaultLogoutRequest(ticketId, selectedService, logoutUrl);
        LOGGER.debug("Logout request [{}] created for [{}] and ticket id [{}]", logoutRequest, selectedService, ticketId);
        final var type = registeredService.getLogoutType() == null
            ? RegisteredService.LogoutType.BACK_CHANNEL : registeredService.getLogoutType();
        LOGGER.debug("Logout type registered for [{}] is [{}]", selectedService, type);

        if (type == RegisteredService.LogoutType.BACK_CHANNEL) {
            if (performBackChannelLogout(logoutRequest)) {
                logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
            } else {
                logoutRequest.setStatus(LogoutRequestStatus.FAILURE);
                LOGGER.warn("Logout message is not sent to [{}]; Continuing processing...", selectedService);
            }
        } else {
            LOGGER.debug("Logout operation is not yet attempted for [{}] given logout type is set to [{}]", selectedService, type);
            logoutRequest.setStatus(LogoutRequestStatus.NOT_ATTEMPTED);
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
            final var logoutRequest = this.logoutMessageBuilder.create(request);
            final var logoutService = request.getService();
            logoutService.setLoggedOutAlready(true);

            LOGGER.debug("Preparing logout request for [{}] to [{}]", logoutService.getId(), request.getLogoutUrl());
            final var msg = new LogoutHttpMessage(request.getLogoutUrl(), logoutRequest, this.asynchronous);
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
}
