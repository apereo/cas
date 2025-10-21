package org.apereo.cas.web.flow.actions;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.web.BrowserStorage;
import org.apereo.cas.web.DefaultBrowserStorage;
import org.apereo.cas.web.cookie.CasCookieBuilder;
import org.apereo.cas.web.flow.BaseWebflowConfigurerTests;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.storage.ReadBrowserStorageAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import tools.jackson.databind.ObjectMapper;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link BrowserStorageActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowAuthenticationActions")
@TestPropertySource(properties = {
    "cas.tgc.pin-to-session=false",
    "cas.tgc.crypto.enabled=false"
})
class BrowserStorageActionTests extends BaseWebflowConfigurerTests {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(false).minimal(false).build().toObjectMapper();

    @Autowired
    @Qualifier(CasCookieBuilder.BEAN_NAME_TICKET_GRANTING_COOKIE_BUILDER)
    private CasCookieBuilder ticketGrantingTicketCookieGenerator;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_WRITE_BROWSER_STORAGE)
    private Action writeSessionStorageAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_READ_BROWSER_STORAGE)
    private Action readSessionStorageAction;

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_PUT_BROWSER_STORAGE)
    private Action putSessionStorageAction;

    @Autowired
    @Qualifier(TicketRegistry.BEAN_NAME)
    private TicketRegistry ticketRegistry;

    @Test
    void verifyPutStorage() throws Exception {
        val context = MockRequestContext.create(applicationContext).withUserAgent("Firefox");
        val request = context.getHttpServletRequest();
        context.setRemoteAddr("185.86.151.11").setLocalAddr("185.88.151.11").setClientInfo();
        val result = putSessionStorageAction.execute(context);
        assertNull(result);
        assertNotNull(WebUtils.getBrowserStoragePayload(request));
    }

    @Test
    void verifyReadFromLocalStorage() throws Exception {
        val ticketGrantingTicket = new MockTicketGrantingTicket("casuser");
        ticketRegistry.addTicket(ticketGrantingTicket);
        
        val context = MockRequestContext.create(applicationContext).withUserAgent("Firefox");
        val request = context.getHttpServletRequest();
        context.setRemoteAddr("185.86.151.11").setLocalAddr("185.88.151.11").setClientInfo();

        var storage = DefaultBrowserStorage.builder()
            .build()
            .setPayloadJson(Map.of(ticketGrantingTicketCookieGenerator.getCookieName(), ticketGrantingTicket.getId()));

        context.setParameter(BrowserStorage.PARAMETER_BROWSER_STORAGE, MAPPER.writeValueAsString(Map.of(storage.getContext(), storage.getPayload())));
        ClientInfoHolder.setClientInfo(ClientInfo.from(request));

        context.setCurrentEvent(new Event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            new LocalAttributeMap<>(TicketGrantingTicket.class.getName(), ticketGrantingTicket.getId())));

        context.getRequestScope().put(BrowserStorage.BrowserStorageTypes.class.getSimpleName(), BrowserStorage.BrowserStorageTypes.LOCAL.name());
        val readResult = readSessionStorageAction.execute(context);
        storage = WebUtils.getBrowserStorage(context);
        assertNotNull(storage);
        assertEquals(BrowserStorage.BrowserStorageTypes.LOCAL, storage.getStorageType());
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, readResult.getId());
        assertNotNull(WebUtils.getTicketGrantingTicketId(context));
        storage = readResult.getAttributes().get(BrowserStorage.PARAMETER_BROWSER_STORAGE, BrowserStorage.class);
        assertNotNull(storage);
    }

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext).withUserAgent("Firefox");
        context.setRemoteAddr("185.86.151.11").setLocalAddr("185.88.151.11").setClientInfo();

        val ticketGrantingTicket = new MockTicketGrantingTicket("casuser");
        context.setCurrentEvent(new Event(this, CasWebflowConstants.TRANSITION_ID_SUCCESS,
            new LocalAttributeMap<>(TicketGrantingTicket.class.getName(), ticketGrantingTicket.getId())));

        var readResult = readSessionStorageAction.execute(context);
        val storage = WebUtils.getBrowserStorage(context);
        assertNotNull(storage);
        assertEquals(BrowserStorage.BrowserStorageTypes.LOCAL, storage.getStorageType());
        assertEquals(CasWebflowConstants.TRANSITION_ID_READ_BROWSER_STORAGE, readResult.getId());
        assertTrue(context.getFlowScope().contains(ReadBrowserStorageAction.BROWSER_STORAGE_REQUEST_IN_PROGRESS));

        val writeResult = writeSessionStorageAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, writeResult.getId());
        assertTrue(context.getFlowScope().contains(BrowserStorage.PARAMETER_BROWSER_STORAGE));

        context.setCurrentEvent(new Event(this, CasWebflowConstants.TRANSITION_ID_CONTINUE));
        val sessionStorage = writeResult.getAttributes().getRequired("result", BrowserStorage.class);
        context.setParameter(BrowserStorage.PARAMETER_BROWSER_STORAGE,
            MAPPER.writeValueAsString(Map.of(sessionStorage.getContext(), sessionStorage.getPayload())));
        readResult = readSessionStorageAction.execute(context);
        assertFalse(context.getFlowScope().contains(ReadBrowserStorageAction.BROWSER_STORAGE_REQUEST_IN_PROGRESS));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, readResult.getId());
        assertEquals(CasWebflowConstants.STATE_ID_TICKET_GRANTING_TICKET_CHECK, WebUtils.getTargetState(context));
        assertNotNull(WebUtils.getTicketGrantingTicketId(context));

        context.getFlowScope().clear();
        context.getRequestScope().clear();
        context.setParameter(BrowserStorage.PARAMETER_BROWSER_STORAGE, StringUtils.EMPTY);
        readResult = readSessionStorageAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_READ_BROWSER_STORAGE, readResult.getId());
        assertNull(WebUtils.getTicketGrantingTicketId(context));
        
        context.getFlowScope().put(ReadBrowserStorageAction.BROWSER_STORAGE_REQUEST_IN_PROGRESS, Boolean.TRUE);
        readResult = readSessionStorageAction.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_SKIP, readResult.getId());
        assertFalse(context.getFlowScope().contains(ReadBrowserStorageAction.BROWSER_STORAGE_REQUEST_IN_PROGRESS));
    }

}
