package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import org.apereo.cas.util.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This logout manager handles the Single Log Out process.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class LogoutManagerImpl implements LogoutManager {

    /** The logger. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutManagerImpl.class);
    
    /** Whether single sign out is disabled or not. */
    private boolean singleLogoutCallbacksDisabled;
    
    private LogoutMessageCreator logoutMessageBuilder;
    
    private SingleLogoutServiceMessageHandler singleLogoutServiceMessageHandler;

    /**
     * Instantiates a new Logout manager.
     */
    public LogoutManagerImpl() {}

    /**
     * Build the logout manager.
     * @param logoutMessageBuilder the builder to construct logout messages.
     */
    public LogoutManagerImpl(final LogoutMessageCreator logoutMessageBuilder) {
        this.logoutMessageBuilder = logoutMessageBuilder;
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
        final List<LogoutRequest> logoutRequests = new ArrayList<>();
        if (this.singleLogoutCallbacksDisabled) {
            LOGGER.info("Single logout callbacks are disabled");
            return logoutRequests;
        }
        performLogoutForTicket(ticket, logoutRequests);
        LOGGER.info("{} logout requests were processed", logoutRequests.size());
        return logoutRequests;
    }

    private void performLogoutForTicket(final TicketGrantingTicket ticket, final List<LogoutRequest> logoutRequests) {
        ticket.getServices().entrySet().stream().filter(entry -> entry.getValue() instanceof SingleLogoutService).forEach(entry -> {
            final Service service = entry.getValue();
            LOGGER.debug("Handling single logout callback for {}", service);
            final LogoutRequest logoutRequest = this.singleLogoutServiceMessageHandler.handle((SingleLogoutService) service,
                    entry.getKey());
            if (logoutRequest != null) {
                LOGGER.debug("Captured logout request [{}]", logoutRequest);
                logoutRequests.add(logoutRequest);
            }
        });

        final Collection<ProxyGrantingTicket> proxyGrantingTickets = ticket.getProxyGrantingTickets();
        if (proxyGrantingTickets.isEmpty()) {
            LOGGER.debug("There are no proxy-granting tickets associated with [{}] to process for single logout", ticket.getId());
        } else {
            proxyGrantingTickets.stream().forEach(proxyGrantingTicket -> performLogoutForTicket(proxyGrantingTicket, logoutRequests));
        }
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

    /**
     * Set if the logout is disabled.
     *
     * @param singleLogoutCallbacksDisabled if the logout is disabled.
     */
    public void setSingleLogoutCallbacksDisabled(final boolean singleLogoutCallbacksDisabled) {
        this.singleLogoutCallbacksDisabled = singleLogoutCallbacksDisabled;
    }

    public void setLogoutMessageBuilder(final LogoutMessageCreator logoutMessageBuilder) {
        this.logoutMessageBuilder = logoutMessageBuilder;
    }

    public void setSingleLogoutServiceMessageHandler(final SingleLogoutServiceMessageHandler singleLogoutServiceMessageHandler) {
        this.singleLogoutServiceMessageHandler = singleLogoutServiceMessageHandler;
    }
    
    public SingleLogoutServiceMessageHandler getSingleLogoutServiceMessageHandler() {
        return this.singleLogoutServiceMessageHandler;
    }
}
