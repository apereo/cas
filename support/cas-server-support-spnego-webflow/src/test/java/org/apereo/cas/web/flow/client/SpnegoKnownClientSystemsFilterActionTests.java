package org.apereo.cas.web.flow.client;

import module java.base;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.util.RegexUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.webflow.action.EventFactorySupport;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for {@link BaseSpnegoKnownClientSystemsFilterAction}
 * and {@link HostNameSpnegoKnownClientSystemsFilterAction}.
 *
 * @author Sean Baker sean.baker@usuhs.edu
 * @author Misagh Moayyed
 * @since 4.1
 */
@Tag("Spnego")
class SpnegoKnownClientSystemsFilterActionTests {

    private static final String ALTERNATE_REMOTE_IP = "74.125.136.102";

    @Test
    void ensureRemoteIpShouldBeChecked() throws Exception {
        val action = new BaseSpnegoKnownClientSystemsFilterAction(RegexUtils.createPattern("^192\\.158\\..+"),
            StringUtils.EMPTY, 0);
        val context = MockRequestContext.create();
        context.setRemoteAddr("192.158.5.781");
        val ev = action.execute(context);
        assertEquals(new EventFactorySupport().yes(this).getId(), ev.getId());
    }

    @Test
    void ensureRemoteIpShouldNotBeChecked() throws Exception {
        val action = new BaseSpnegoKnownClientSystemsFilterAction(RegexUtils.createPattern("^192\\.158\\..+"),
            StringUtils.EMPTY, 0);
        val context = MockRequestContext.create();
        context.setRemoteAddr("193.158.5.781");
        val ev = action.execute(context);
        assertNotEquals(new EventFactorySupport().yes(this).getId(), ev.getId());
    }

    @Test
    void ensureAltRemoteIpHeaderShouldBeChecked() throws Exception {
        val action = new BaseSpnegoKnownClientSystemsFilterAction(RegexUtils.createPattern("^74\\.125\\..+"),
            "alternateRemoteIp", 120);

        val context = MockRequestContext.create();
        context.setRemoteAddr("555.555.555.555");
        context.addHeader("alternateRemoteIp", ALTERNATE_REMOTE_IP);
        val ev = action.execute(context);
        assertEquals(new EventFactorySupport().yes(this).getId(), ev.getId());
    }

    @Test
    void ensureHostnameShouldDoSpnego() throws Exception {
        val action = new HostNameSpnegoKnownClientSystemsFilterAction(RegexUtils.createPattern(".+"),
            StringUtils.EMPTY, 0, "\\w+\\.\\w+\\.\\w+");

        val context = MockRequestContext.create();
        context.setRemoteAddr(ALTERNATE_REMOTE_IP);
        val ev = action.execute(context);
        assertEquals(new EventFactorySupport().yes(this).getId(), ev.getId());
    }

    @Test
    void ensureHostnameAndIpShouldDoSpnego() throws Exception {
        val action = new HostNameSpnegoKnownClientSystemsFilterAction(RegexUtils.createPattern("74\\..+"),
            StringUtils.EMPTY, 0, "\\w+\\.\\w+\\.\\w+");

        val context = MockRequestContext.create();
        context.setRemoteAddr(ALTERNATE_REMOTE_IP);
        val ev = action.execute(context);
        assertEquals(new EventFactorySupport().yes(this).getId(), ev.getId());

    }

    @Test
    void verifyIpMismatchWhenCheckingHostnameForSpnego() throws Throwable {
        val action = new HostNameSpnegoKnownClientSystemsFilterAction(RegexUtils.createPattern("14\\..+"),
            StringUtils.EMPTY, 0, "\\w+\\.\\w+\\.\\w+");
        val context = MockRequestContext.create();
        context.setRemoteAddr(ALTERNATE_REMOTE_IP);
        val ev = action.execute(context);
        assertEquals(new EventFactorySupport().no(this).getId(), ev.getId());

    }
}
