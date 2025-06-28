package org.apereo.cas.web.flow;

import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import jakarta.servlet.http.Cookie;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TerminateSessionActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("WebflowActions")
class TerminateSessionActionTests {
    @Nested
    @TestPropertySource(properties = "cas.tgc.crypto.enabled=false")
    class DefaultTests extends AbstractWebflowActionsTests {

        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_TERMINATE_SESSION)
        private Action action;

        @Test
        void verifyTerminateAction() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            WebUtils.putTicketGrantingTicketInScopes(context, "TGT-123456-something");
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.execute(context).getId());
            assertNotNull(WebUtils.getLogoutRequests(context));
        }

        @Test
        void verifyTerminateActionByCookie() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setHttpRequestCookies(new Cookie("TGC", "TGT-123456-something"));
            assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, action.execute(context).getId());
            assertNotNull(WebUtils.getLogoutRequests(context));
        }
    }

    @Nested
    @TestPropertySource(properties = {
        "cas.tgc.crypto.enabled=false",
        "cas.logout.confirm-logout=true",
        "cas.logout.redirect-url=https://github.com"
    })
    class ConfirmingLogoutTests extends AbstractWebflowActionsTests {
        @Autowired
        @Qualifier(CasWebflowConstants.ACTION_ID_TERMINATE_SESSION)
        private Action action;

        @Test
        void verifyTerminateActionConfirmed() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            context.setParameter(WebUtils.REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED, "true");
            WebUtils.putTicketGrantingTicketInScopes(context, "TGT-123456-something");
            assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, action.execute(context).getId());
        }

        @Test
        void verifyTerminateActionRequests() throws Throwable {
            val tgt = new MockTicketGrantingTicket(RegisteredServiceTestUtils.getAuthentication());
            getTicketRegistry().addTicket(tgt);
            val context = MockRequestContext.create(applicationContext);
            context.setParameter(WebUtils.REQUEST_PARAM_LOGOUT_REQUEST_CONFIRMED, "true");
            WebUtils.putTicketGrantingTicketInScopes(context, tgt.getId());
            WebUtils.putAuthentication(tgt.getAuthentication(), context);
            assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, action.execute(context).getId());
            assertNull(getTicketRegistry().getTicket(tgt.getId()));
            assertTrue(WebUtils.getLogoutRequests(context).isEmpty());
        }

        @Test
        void verifyTerminateActionConfirming() throws Throwable {
            val context = MockRequestContext.create(applicationContext);
            WebUtils.putTicketGrantingTicketInScopes(context, "TGT-123456-something");
            assertEquals(CasWebflowConstants.STATE_ID_WARN, action.execute(context).getId());
        }
    }
}
