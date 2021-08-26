package org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FinishLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebflowActions")
public class FinishLogoutActionTests extends AbstractWebflowActionsTests {

    private static final String URL = "https://ww.google.com";

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_FINISH_LOGOUT)
    private Action action;

    @Test
    public void verifyLogout() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
            new MockHttpServletRequest(), new MockHttpServletResponse()));
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, result.getId());
    }

    @Test
    public void verifyLogoutRedirect() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
            new MockHttpServletRequest(), new MockHttpServletResponse()));
        WebUtils.putLogoutRedirectUrl(context, URL);
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, result.getId());
    }

    @Test
    public void verifyLogoutPost() throws Exception {
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
                new MockHttpServletRequest(), new MockHttpServletResponse()));
        WebUtils.putLogoutPostUrl(context, URL);
        val data = new HashMap<String, Object>();
        data.put("SAMLResponse", "xyz");
        WebUtils.putLogoutPostData(context, data);
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_POST, result.getId());
        assertEquals(URL, context.getFlowScope().get("originalUrl"));
        assertEquals(data, context.getFlowScope().get("parameters"));
    }
}
