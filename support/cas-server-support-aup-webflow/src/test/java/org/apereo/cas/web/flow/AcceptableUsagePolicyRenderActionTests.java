package org.apereo.cas.web.flow;

import org.apereo.cas.aup.AcceptableUsagePolicyTerms;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.mock.MockTicketGrantingTicket;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This is {@link AcceptableUsagePolicyRenderActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebflowAccountActions")
@TestPropertySource(properties = "cas.acceptable-usage-policy.core.aup-policy-terms-attribute-name=cn")
class AcceptableUsagePolicyRenderActionTests extends BaseAcceptableUsagePolicyActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_AUP_RENDER)
    private Action acceptableUsagePolicyRenderAction;

    @Test
    void verifyAction() throws Throwable {

        val appContext = mock(ApplicationContext.class);
        when(appContext.getMessage(anyString(), any(), anyString(), any(Locale.class))).thenReturn("hello.world");
        val context = MockRequestContext.create(appContext);
        WebUtils.putCredential(context, CoreAuthenticationTestUtils.getCredentialsWithSameUsernameAndPassword());
        WebUtils.putTicketGrantingTicketInScopes(context, new MockTicketGrantingTicket("casuser"));
        WebUtils.putAuthentication(CoreAuthenticationTestUtils.getAuthentication(), context);
        assertNull(acceptableUsagePolicyRenderAction.execute(context));
        val terms = WebUtils.getAcceptableUsagePolicyTermsFromFlowScope(context, AcceptableUsagePolicyTerms.class);
        assertNotNull(terms);
        assertTrue(terms.getCode().startsWith(AcceptableUsagePolicyTerms.CODE));
    }
}

