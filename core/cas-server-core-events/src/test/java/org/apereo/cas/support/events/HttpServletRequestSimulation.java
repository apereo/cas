package org.apereo.cas.support.events;


import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.Callable;


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
public class HttpServletRequestSimulation implements Callable<Integer> {

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

    private final ConfigurableApplicationContext applicationContext;
    
    /**
     * Create an instance of the HttpServletRequestSimulation callable object.
     *
     * @param threadNum          The thread number of the current request.  This is the value that will be returned after call is completed.
     * @param useIP1             A boolean if set to true will use the constant IP1 for the client/server IP addresses.  Set to false it will use IP2.
     * @param applicationContext The Spring applicationContext the test is running under.  This is to publish the event to the context.
     */
    public HttpServletRequestSimulation(final Integer threadNum, final boolean useIP1, final ConfigurableApplicationContext applicationContext) {
        this.threadNum = threadNum;
        this.useIP1 = useIP1;
        this.applicationContext = applicationContext;
    }

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
        val tgt = new MockTicketGrantingTicket("casuser");
        val event = new CasTicketGrantingTicketCreatedEvent(this, tgt, ClientInfoHolder.getClientInfo());
        applicationContext.publishEvent(event);
    }
}
