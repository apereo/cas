package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This logout manager handles the Single Log Out process.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class DefaultLogoutManager implements LogoutManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLogoutManager.class);

    private final boolean singleLogoutCallbacksDisabled;
    private final LogoutMessageCreator logoutMessageBuilder;
    private final SingleLogoutServiceMessageHandler singleLogoutServiceMessageHandler;
    private final LogoutExecutionPlan logoutExecutionPlan;

    /**
     * Build the logout manager.
     *
     * @param logoutMessageBuilder              the builder to construct logout messages.
     * @param singleLogoutServiceMessageHandler who actually perform the logout request
     * @param singleLogoutCallbacksDisabled     Set if the logout is disabled.
     * @param logoutExecutionPlan               the logout execution plan
     */
    public DefaultLogoutManager(final LogoutMessageCreator logoutMessageBuilder, final SingleLogoutServiceMessageHandler singleLogoutServiceMessageHandler,
                                final boolean singleLogoutCallbacksDisabled, final LogoutExecutionPlan logoutExecutionPlan) {
        this.logoutMessageBuilder = logoutMessageBuilder;
        this.singleLogoutServiceMessageHandler = singleLogoutServiceMessageHandler;
        this.singleLogoutCallbacksDisabled = singleLogoutCallbacksDisabled;
        this.logoutExecutionPlan = logoutExecutionPlan;
    }

    /**
     * Perform a back channel logout for a given ticket granting ticket and returns all the logout requests.
     *
     * @param ticket a given ticket granting ticket.
     * @return all logout requests.
     */
    @Override
    public List<LogoutRequest> performLogout(final TicketGrantingTicket ticket) {
        LOGGER.info("Performing logout operations for [{}]", ticket.getId());
        if (this.singleLogoutCallbacksDisabled) {
            LOGGER.info("Single logout callbacks are disabled");
            return new ArrayList<>(0);
        }
        final List<LogoutRequest> logoutRequests = performLogoutForTicket(ticket);
        this.logoutExecutionPlan.getLogoutHandlers().forEach(h -> {
            LOGGER.debug("Invoking logout handler [{}] to process ticket [{}]", h.getClass().getSimpleName(), ticket.getId());
            h.handle(ticket);
        });
        LOGGER.info("[{}] logout requests were processed", logoutRequests.size());
        return logoutRequests;
    }

    private List<LogoutRequest> performLogoutForTicket(final TicketGrantingTicket ticketToBeLoggedOut) {
        final Stream<Map<String, Service>> streamServices = Stream.concat(Stream.of(ticketToBeLoggedOut.getServices()),
                Stream.of(ticketToBeLoggedOut.getProxyGrantingTickets()));
        return streamServices
                .map(Map::entrySet)
                .flatMap(Set::stream)
                .filter(entry -> entry.getValue() instanceof WebApplicationService)
                .map(entry -> {
                    final Service service = entry.getValue();
                    LOGGER.debug("Handling single logout callback for [{}]", service);
                    return this.singleLogoutServiceMessageHandler.handle((WebApplicationService) service, entry.getKey());
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Create a logout message for front channel logout.
     *
     * @param logoutRequest the logout request.
     * @return a front SAML logout message.
     */
    @Override
    public String createFrontChannelLogoutMessage(final LogoutRequest logoutRequest) {
        final String logoutMessage = this.logoutMessageBuilder.create(logoutRequest);
        LOGGER.trace("Attempting to deflate the logout message [{}]", logoutMessage);
        return CompressionUtils.deflate(logoutMessage);
    }
}
