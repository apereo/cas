package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.logout.slo.SingleLogoutContinuation;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.flow.logout.LogoutViewSetupAction;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.view.DynamicHtmlView;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LogoutViewSetupActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebflowActions")
class LogoutViewSetupActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_LOGOUT_VIEW_SETUP)
    private Action logoutViewSetupAction;

    @Test
    void verifyOperation() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.setRequestAttribute(SingleLogoutContinuation.class.getName(),
            SingleLogoutContinuation.builder().content("Testing").build());
        val results = logoutViewSetupAction.execute(context);
        assertNull(results);
        assertFalse(WebUtils.isGeoLocationTrackingIntoFlowScope(context));
        assertEquals(HttpStatus.OK.value(), context.getHttpServletResponse().getStatus());
        assertTrue(context.getFlowScope().contains(DynamicHtmlView.class.getName()));
        assertTrue(context.getFlowScope().contains(LogoutViewSetupAction.FLOW_SCOPE_ATTRIBUTE_PROCEED));
    }

    @Test
    void verifyOperationWithSloContinuationInScope() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        context.getConversationScope().put(SingleLogoutContinuation.class.getName(),
            SingleLogoutContinuation.builder().content("Testing").build());
        val results = logoutViewSetupAction.execute(context);
        assertNull(results);
        assertFalse(WebUtils.isGeoLocationTrackingIntoFlowScope(context));
        assertEquals(HttpStatus.OK.value(), context.getHttpServletResponse().getStatus());
        assertTrue(context.getFlowScope().contains(DynamicHtmlView.class.getName()));
        assertTrue(context.getFlowScope().contains(LogoutViewSetupAction.FLOW_SCOPE_ATTRIBUTE_PROCEED));
    }
}
