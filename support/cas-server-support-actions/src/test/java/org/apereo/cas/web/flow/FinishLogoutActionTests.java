package org.apereo.cas.web.flow;

import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link FinishLogoutActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebflowActions")
class FinishLogoutActionTests extends AbstractWebflowActionsTests {

    private static final String URL = "https://ww.google.com";

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_FINISH_LOGOUT)
    private Action action;

    @Test
    void verifyLogout() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_FINISH, result.getId());
    }

    @Test
    void verifyLogoutRedirect() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
        WebUtils.putLogoutRedirectUrl(context, URL);
        val result = action.execute(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_REDIRECT, result.getId());
    }

    @Test
    void verifyLogoutPost() throws Throwable {
        val context = MockRequestContext.create(applicationContext);
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
