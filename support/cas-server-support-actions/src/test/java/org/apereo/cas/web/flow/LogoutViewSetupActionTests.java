package org.apereo.cas.web.flow;

import org.apereo.cas.web.flow.logout.LogoutViewSetupAction;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.cas.web.view.DynamicHtmlView;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.exception.http.HttpAction;
import org.pac4j.core.exception.http.OkAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LogoutViewSetupActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebflowActions")
public class LogoutViewSetupActionTests extends AbstractWebflowActionsTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_LOGOUT_VIEW_SETUP)
    private Action logoutViewSetupAction;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        request.setAttribute(HttpAction.class.getName(), new OkAction("Test"));
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        val results = logoutViewSetupAction.execute(context);
        assertNull(results);
        assertFalse(WebUtils.isGeoLocationTrackingIntoFlowScope(context));
        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertTrue(context.getFlowScope().contains(DynamicHtmlView.class.getName()));
        assertTrue(context.getFlowScope().contains(LogoutViewSetupAction.FLOW_SCOPE_ATTRIBUTE_PROCEED));
    }
}
