package org.apereo.cas.support.events;

import module java.base;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;


/**
 * This is {@link HttpServletRequestSimulation}.
 * <p>
 * This {@link Callable} class simulates a thread that an application server would create for
 * incoming HttpServletRequests, and will publish a {@link CasTicketGrantingTicketCreatedEvent}
 * to the application context. It will populate the {@link ClientInfo} object for each 'thread' with data from the {@link HttpServletRequest}.
 * This is meant to be used for testing with the {@link CasAuthenticationEventListenerTests} class.
 * It will set the ips of the server/client to either IP1 or IP2 depending on the constructor value. *
 *
 * @author David Malia
 * @since 6.6.6
 */
@RequiredArgsConstructor
class HttpServletRequestSimulation implements Callable<Integer> {

    /**
     * A constant representing an IP address.
     */
    public static final String IP1 = "123.12.123.123";

    /**
     * A constant representing an IP address.
     */
    public static final String IP2 = "123.45.67.89";

    private final Integer threadNum;
    private final boolean useIP1;

    private final String principalId;

    private final ConfigurableApplicationContext applicationContext;
    
    @Override
    public Integer call() {
        postTGTCreatedEvent();
        return threadNum;
    }

    /**
     * Create a {@link MockHttpServletRequest},
     * set the IP, then create {@link MockTicketGrantingTicket}, then post the
     * {@link CasTicketGrantingTicketCreatedEvent} event to the application context for processing.
     */
    private void postTGTCreatedEvent() {
        val request = new MockHttpServletRequest();
        if (useIP1) {
            request.setRemoteAddr(IP1);
            request.setLocalAddr(IP1);
        } else {
            request.setRemoteAddr(IP2);
            request.setLocalAddr(IP2);
        }
        request.addHeader(HttpHeaders.USER_AGENT, "test");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));
        val tgt = new MockTicketGrantingTicket(principalId);
        val event = new CasTicketGrantingTicketCreatedEvent(this, tgt, ClientInfoHolder.getClientInfo());
        applicationContext.publishEvent(event);
    }
}
