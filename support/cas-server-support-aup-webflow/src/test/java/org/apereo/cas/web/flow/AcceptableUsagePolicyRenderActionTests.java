package org.apereo.cas.web.flow;

import org.apereo.cas.aup.AcceptableUsagePolicyTerms;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.web.support.WebUtils;

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptableUsagePolicyRenderActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@DirtiesContext
@Tag("Webflow")
@TestPropertySource(properties = "cas.acceptable-usage-policy.aup-policy-terms-attribute-name=cn")
public class AcceptableUsagePolicyRenderActionTests extends BaseAcceptableUsagePolicyActionTests {

    @Autowired
    @Qualifier("acceptableUsagePolicyRenderAction")
    private Action acceptableUsagePolicyRenderAction;

    @Test
    @SneakyThrows
    public void verifyAction() {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
            request, new MockHttpServletResponse()));
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket("casuser"));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        assertNull(acceptableUsagePolicyRenderAction.execute(context));
        val terms = WebUtils.getAcceptableUsagePolicyTermsFromFlowScope(context, AcceptableUsagePolicyTerms.class);
        assertNotNull(terms);
        assertTrue(terms.getCode().startsWith(AcceptableUsagePolicyTerms.CODE));
    }
}

