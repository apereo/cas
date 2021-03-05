package org.apereo.cas.qr.web.flow;

import org.apereo.cas.qr.BaseQRAuthenticationTokenValidatorServiceTests;

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

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QRAuthenticationGenerateCodeActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
@SpringBootTest(classes = BaseQRAuthenticationTokenValidatorServiceTests.SharedTestConfiguration.class)
public class QRAuthenticationGenerateCodeActionTests {
    @Autowired
    @Qualifier("qrAuthenticationGenerateCodeAction")
    private Action qrAuthenticationGenerateCodeAction;

    @Test
    public void verifyOperation() throws Exception {
        val context = new MockRequestContext();
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        RequestContextHolder.setRequestContext(context);
        ExternalContextHolder.setExternalContext(context.getExternalContext());

        val result = qrAuthenticationGenerateCodeAction.execute(context);
        assertNull(result);

        assertTrue(context.getFlowScope().contains("qrCode"));
        assertTrue(context.getFlowScope().contains("qrChannel"));
        assertTrue(context.getFlowScope().contains("qrPrefix"));
    }

}
