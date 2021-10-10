package org.apereo.cas.web.flow;

import org.apereo.cas.aup.AcceptableUsagePolicyTerms;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AcceptableUsagePolicyRenderActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowActions")
@TestPropertySource(properties = "cas.acceptable-usage-policy.core.aup-policy-terms-attribute-name=cn")
public class AcceptableUsagePolicyRenderActionTests extends BaseAcceptableUsagePolicyActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_AUP_RENDER)
    private Action acceptableUsagePolicyRenderAction;

    @Test
    public void verifyAction() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(),
            request, new MockHttpServletResponse()));

        val appContext = mock(ApplicationContext.class);
        when(appContext.getMessage(anyString(), any(), anyString(), any(Locale.class)))
            .thenReturn("hello.world");

        ((Flow) context.getActiveFlow()).setApplicationContext(appContext);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket("casuser"));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        assertNull(acceptableUsagePolicyRenderAction.execute(context));
        val terms = WebUtils.getAcceptableUsagePolicyTermsFromFlowScope(context, AcceptableUsagePolicyTerms.class);
        assertNotNull(terms);
        assertTrue(terms.getCode().startsWith(AcceptableUsagePolicyTerms.CODE));
    }
}

