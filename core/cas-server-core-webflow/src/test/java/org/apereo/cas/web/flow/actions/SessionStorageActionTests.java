package org.apereo.cas.web.flow.actions;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.BrowserSessionStorage;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SessionStorageActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowAuthenticationActions")
public class SessionStorageActionTests extends BaseWebflowConfigurerTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WRITE_SESSION_STORAGE)
    private Action writeSessionStorageAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_READ_SESSION_STORAGE)
    private Action readSessionStorageAction;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext).withUserAgent("Firefox");
        val request = context.getHttpServletRequest();
        request.setRemoteAddr("185.86.151.11");
        request.setLocalAddr("185.88.151.11");
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        val ticketGrantingTicket = new MockTicketGrantingTicket("casuser");
        context.setCurrentEvent(new Event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            new LocalAttributeMap<>(TicketGrantingTicket.class.getName(), ticketGrantingTicket.getId())));

        var readResult = readSessionStorageAction.execute(context);
        assertTrue(context.getFlowScope().contains(BrowserSessionStorage.KEY_SESSION_STORAGE_CONTEXT));
        assertEquals(CasWebflowConstants.TRANSITION_ID_READ_SESSION_STORAGE, readResult.getId());

        val writeResult = writeSessionStorageAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, writeResult.getId());
        assertTrue(context.getFlowScope().contains(BrowserSessionStorage.KEY_SESSION_STORAGE));

        context.setCurrentEvent(new Event(this, CasWebflowConstants.TRANSITION_ID_CONTINUE));
        val sessionStorage = writeResult.getAttributes().getRequired("result", BrowserSessionStorage.class);
        context.setParameter(BrowserSessionStorage.KEY_SESSION_STORAGE, sessionStorage.getPayload());
        readResult = readSessionStorageAction.execute(context);
        assertNull(readResult);
        assertNotNull(WebUtils.getTicketGrantingTicketId(context));

        context.getFlowScope().clear();
        context.getRequestScope().clear();
        context.setParameter(BrowserSessionStorage.KEY_SESSION_STORAGE, StringUtils.EMPTY);
        readResult = readSessionStorageAction.execute(context);
        assertNull(readResult);
        assertNull(WebUtils.getTicketGrantingTicketId(context));
    }

}
