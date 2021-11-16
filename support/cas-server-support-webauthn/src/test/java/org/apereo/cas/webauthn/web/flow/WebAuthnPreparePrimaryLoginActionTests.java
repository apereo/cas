package org.apereo.cas.webauthn.web.flow;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.ExternalContextHolder;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This is {@link WebAuthnPreparePrimaryLoginActionTests}.
 *
 * @author Jerome Leleu
 * @since 6.5.0
 */
@Tag("WebflowMfaActions")
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class)
public class WebAuthnPreparePrimaryLoginActionTests {

    @Autowired
    @Qualifier("webAuthnPreparePrimaryLoginAction")
    private Action webAuthnPreparePrimaryLoginAction;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        webAuthnPreparePrimaryLoginAction.execute(context);
        assertTrue(context.getFlowScope().contains("_csrf"));
    }

}
