package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.logout.DefaultSingleLogoutRequest;
import org.apereo.cas.logout.LogoutHttpMessage;
import org.apereo.cas.logout.LogoutRequestStatus;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceLogoutType;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.http.HttpClient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This is {@link BaseSingleLogoutServiceMessageHandler}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public abstract class BaseSingleLogoutServiceMessageHandler implements SingleLogoutServiceMessageHandler {
    private final HttpClient httpClient;

    private final SingleLogoutMessageCreator logoutMessageBuilder;

    private final ServicesManager servicesManager;

    private final SingleLogoutServiceLogoutUrlBuilder singleLogoutServiceLogoutUrlBuilder;

    private final boolean asynchronous;

    private final AuthenticationServiceSelectionPlan authenticationRequestServiceSelectionStrategies;

    @Override
    public Collection<SingleLogoutRequest> handle(final WebApplicationService singleLogoutService, final String ticketId,
                                                  final TicketGrantingTicket ticketGrantingTicket) {
        if (singleLogoutService.isLoggedOutAlready()) {
            LOGGER.debug("Service [{}] is already logged out.", singleLogoutService);
            return new ArrayList<>(0);
        }
        val selectedService = (WebApplicationService) this.authenticationRequestServiceSelectionStrategies.resolveService(singleLogoutService);

        LOGGER.trace("Processing logout request for service [{}]...", selectedService);
        val registeredService = this.servicesManager.findServiceBy(selectedService);

        LOGGER.debug("Service [{}] supports single logout and is found in the registry as [{}]. Proceeding...",
            selectedService.getId(), registeredService.getName());

        val logoutUrls = this.singleLogoutServiceLogoutUrlBuilder.determineLogoutUrl(registeredService, selectedService);
        LOGGER.debug("Prepared logout url [{}] for service [{}]", logoutUrls, selectedService);
        if (logoutUrls == null || logoutUrls.isEmpty()) {
            LOGGER.debug("Service [{}] does not support logout operations given no logout url could be determined.", selectedService);
            return new ArrayList<>(0);
        }

        LOGGER.trace("Creating logout request for [{}] and ticket id [{}]", selectedService, ticketId);
        return createLogoutRequests(ticketId, selectedService, registeredService, logoutUrls, ticketGrantingTicket);
    }

    @Override
    public boolean supports(final WebApplicationService singleLogoutService) {
        val selectedService = (WebApplicationService) this.authenticationRequestServiceSelectionStrategies.resolveService(singleLogoutService);
        val registeredService = this.servicesManager.findServiceBy(selectedService);

        if (registeredService != null
            && registeredService.getAccessStrategy().isServiceAccessAllowed()
            && registeredService.getLogoutType() != RegisteredServiceLogoutType.NONE) {
            return supportsInternal(singleLogoutService, registeredService);
        }
        return false;
    }

    @Override
    public boolean performBackChannelLogout(final SingleLogoutRequest request) {
        try {
            LOGGER.trace("Creating back-channel logout request based on [{}]", request);
            val logoutRequest = createSingleLogoutMessage(request);
            return sendSingleLogoutMessage(request, logoutRequest);
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.error(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return false;
    }

    @Override
    public SingleLogoutMessage createSingleLogoutMessage(final SingleLogoutRequest logoutRequest) {
        return this.logoutMessageBuilder.create(logoutRequest);
    }

    /**
     * Supports internal.
     *
     * @param singleLogoutService the single logout service
     * @param registeredService   the registered service
     * @return true/false
     */
    protected boolean supportsInternal(final WebApplicationService singleLogoutService, final RegisteredService registeredService) {
        return true;
    }

    /**
     * Create logout requests collection.
     *
     * @param ticketId             the ticket id
     * @param selectedService      the selected service
     * @param registeredService    the registered service
     * @param logoutUrls           the logout urls
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the collection
     */
    protected Collection<SingleLogoutRequest> createLogoutRequests(final String ticketId,
                                                                   final WebApplicationService selectedService,
                                                                   final RegisteredService registeredService,
                                                                   final Collection<SingleLogoutUrl> logoutUrls,
                                                                   final TicketGrantingTicket ticketGrantingTicket) {
        return logoutUrls
            .stream()
            .map(url -> createLogoutRequest(ticketId, selectedService, registeredService, url, ticketGrantingTicket))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * Create logout request logout request.
     *
     * @param ticketId             the ticket id
     * @param selectedService      the selected service
     * @param registeredService    the registered service
     * @param logoutUrl            the logout url
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the logout request
     */
    @SneakyThrows
    protected SingleLogoutRequest createLogoutRequest(final String ticketId,
                                                      final WebApplicationService selectedService,
                                                      final RegisteredService registeredService,
                                                      final SingleLogoutUrl logoutUrl,
                                                      final TicketGrantingTicket ticketGrantingTicket) {
        val logoutRequest = DefaultSingleLogoutRequest.builder()
            .ticketId(ticketId)
            .service(selectedService)
            .logoutUrl(new URL(logoutUrl.getUrl()))
            .logoutType(logoutUrl.getLogoutType())
            .registeredService(registeredService)
            .ticketGrantingTicket(ticketGrantingTicket)
            .properties(logoutUrl.getProperties())
            .build();

        LOGGER.trace("Logout request [{}] created for [{}] and ticket id [{}]", logoutRequest, selectedService, ticketId);
        if (logoutRequest.getLogoutType() == RegisteredServiceLogoutType.BACK_CHANNEL) {
            if (performBackChannelLogout(logoutRequest)) {
                logoutRequest.setStatus(LogoutRequestStatus.SUCCESS);
            } else {
                logoutRequest.setStatus(LogoutRequestStatus.FAILURE);
                LOGGER.warn("Logout message is not sent to [{}]; Continuing processing...", selectedService);
            }
        } else {
            LOGGER.trace("Logout operation is not yet attempted for [{}] given logout type is set to [{}]", selectedService, logoutRequest.getLogoutType());
            logoutRequest.setStatus(LogoutRequestStatus.NOT_ATTEMPTED);
        }
        return logoutRequest;
    }

    /**
     * Send single logout message.
     *
     * @param request       the request
     * @param logoutMessage the logout request
     * @return true if the message was successfully sent.
     */
    protected boolean sendSingleLogoutMessage(final SingleLogoutRequest request, final SingleLogoutMessage logoutMessage) {
        val logoutService = request.getService();
        LOGGER.trace("Preparing logout request for [{}] to [{}]", logoutService.getId(), request.getLogoutUrl());
        val msg = getLogoutHttpMessageToSend(request, logoutMessage);
        LOGGER.debug("Prepared logout message to send is [{}]. Sending...", msg);
        val result = sendMessageToEndpoint(msg, request, logoutMessage);
        logoutService.setLoggedOutAlready(result);
        return result;
    }

    /**
     * Send message to endpoint.
     *
     * @param msg           the msg
     * @param request       the request
     * @param logoutMessage the logout message
     * @return true/false
     */
    protected boolean sendMessageToEndpoint(final LogoutHttpMessage msg, final SingleLogoutRequest request, final SingleLogoutMessage logoutMessage) {
        return this.httpClient.sendMessageToEndPoint(msg);
    }

    /**
     * Gets logout http message to send.
     *
     * @param request       the request
     * @param logoutMessage the logout message
     * @return the logout http message to send
     */
    protected LogoutHttpMessage getLogoutHttpMessageToSend(final SingleLogoutRequest request, final SingleLogoutMessage logoutMessage) {
        return new LogoutHttpMessage(request.getLogoutUrl(), logoutMessage.getPayload(), this.asynchronous);
    }
}
