package org.apereo.cas.web.flow.client;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * Test cases for {@link BaseSpnegoKnownClientSystemsFilterAction}
 * and {@link HostNameSpnegoKnownClientSystemsFilterAction}.
 * @author Sean Baker sean.baker@usuhs.edu
 * @author Misagh Moayyed
 * @since 4.1
 */
public class AllSpnegoKnownClientSystemsFilterActionTests {

    private static final String ALTERNATE_REMOTE_IP = "74.125.136.102";

    @Test
    public void ensureRemoteIpShouldBeChecked() {
        final BaseSpnegoKnownClientSystemsFilterAction action =
                new BaseSpnegoKnownClientSystemsFilterAction("^192\\.158\\..+", "", 0);

        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("192.158.5.781");
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().yes(this).getId());
    }

    @Test
    public void ensureRemoteIpShouldNotBeChecked() {
        final BaseSpnegoKnownClientSystemsFilterAction action =
                new BaseSpnegoKnownClientSystemsFilterAction("^192\\.158\\..+", "", 0);

        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("193.158.5.781");
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertNotEquals(ev.getId(), new EventFactorySupport().yes(this).getId());
    }

    @Test
    public void ensureAltRemoteIpHeaderShouldBeChecked() {
        final BaseSpnegoKnownClientSystemsFilterAction action =
                new BaseSpnegoKnownClientSystemsFilterAction("^74\\.125\\..+", "alternateRemoteIp", 120);

        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr("555.555.555.555");
        req.addHeader("alternateRemoteIp", ALTERNATE_REMOTE_IP);
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().yes(this).getId());
    }

    @Test
    public void ensureHostnameShouldDoSpnego() {
        final HostNameSpnegoKnownClientSystemsFilterAction action =
                new HostNameSpnegoKnownClientSystemsFilterAction("", "", 0, "\\w+\\.\\w+\\.\\w+");

        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr(ALTERNATE_REMOTE_IP);
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().yes(this).getId());

    }

    @Test
    public void ensureHostnameAndIpShouldDoSpnego() {
        final HostNameSpnegoKnownClientSystemsFilterAction action =
                new HostNameSpnegoKnownClientSystemsFilterAction("74\\..+", "", 0, "\\w+\\.\\w+\\.\\w+");

        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr(ALTERNATE_REMOTE_IP);
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().yes(this).getId());

    }

    @Test
    public void verifyIpMismatchWhenCheckingHostnameForSpnego() {
        final HostNameSpnegoKnownClientSystemsFilterAction action =
                new HostNameSpnegoKnownClientSystemsFilterAction("14\\..+", "", 0, "\\w+\\.\\w+\\.\\w+");

        final MockRequestContext ctx = new MockRequestContext();
        final MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRemoteAddr(ALTERNATE_REMOTE_IP);
        final ServletExternalContext extCtx = new ServletExternalContext(
                new MockServletContext(), req,
                new MockHttpServletResponse());
        ctx.setExternalContext(extCtx);

        final Event ev = action.doExecute(ctx);
        assertEquals(ev.getId(), new EventFactorySupport().no(this).getId());

    }
}
