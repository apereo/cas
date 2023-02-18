package org.apereo.cas.support.events;

import lombok.val;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.support.events.ticket.CasTicketGrantingTicketCreatedEvent;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.concurrent.Callable;

public class HttpServletRequestSimulation implements Callable<Integer> {

    private final Integer threadNum;
    private final boolean useIP1;

    private ConfigurableApplicationContext applicationContext;

    public static final String IP1 = "123.12.123.123";
    public static final String IP2 = "123.45.67.89";

    public HttpServletRequestSimulation(Integer threadNum, boolean useIP1, ConfigurableApplicationContext applicationContext) {
        this.threadNum = threadNum;
        this.useIP1 = useIP1;
        this.applicationContext = applicationContext;
    }

    @Override
    public Integer call() throws Exception {
        postTGTCreatedEvent();
        return threadNum;
    }

    private void postTGTCreatedEvent() {
        val request = new MockHttpServletRequest();
        if(useIP1) {
            request.setRemoteAddr(IP1);
            request.setLocalAddr(IP1);
        } else {
            request.setRemoteAddr(IP2);
            request.setLocalAddr(IP2);
        }
        request.addHeader(HttpRequestUtils.USER_AGENT_HEADER, "test");
        ClientInfoHolder.setClientInfo(new ClientInfo(request));
        val tgt = new MockTicketGrantingTicket("casuser");
        val event = new CasTicketGrantingTicketCreatedEvent(this, tgt);
        applicationContext.publishEvent(event);
    }
}
