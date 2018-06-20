package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link AcceptableUsagePolicySubmitActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@DirtiesContext
public class AcceptableUsagePolicySubmitActionTests extends BaseAcceptableUsagePolicyActionTests {

    @Autowired
    @Qualifier("acceptableUsagePolicySubmitAction")
    private Action acceptableUsagePolicySubmitAction;

    @Test
    public void verifyAction() throws Exception {
        final var context = new MockRequestContext();
        final var request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));

        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals("success", acceptableUsagePolicySubmitAction.execute(context).getId());
    }
}
