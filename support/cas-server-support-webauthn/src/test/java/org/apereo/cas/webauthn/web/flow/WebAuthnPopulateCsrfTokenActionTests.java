package org.apereo.cas.webauthn.web.flow;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockRequestContext;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.webflow.execution.Action;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnPopulateCsrfTokenActionTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebflowMfaActions")
@ExtendWith(CasTestExtension.class)
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class)
class WebAuthnPopulateCsrfTokenActionTests {
    @Autowired
    @Qualifier("webAuthnPopulateCsrfTokenAction")
    private Action webAuthnPopulateCsrfTokenAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        val request = (MockHttpServletRequest) WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val response = (MockHttpServletResponse) WebUtils.getHttpServletResponseFromExternalWebflowContext(context);

        webAuthnPopulateCsrfTokenAction.execute(context);
        val csrf1 = context.getFlowScope().get("_csrf", CsrfToken.class);
        assertNotNull(csrf1);
        assertEquals("X-CSRF-TOKEN", csrf1.getHeaderName());

        request.setCookies(response.getCookie("XSRF-TOKEN"));

        webAuthnPopulateCsrfTokenAction.execute(context);
        val csrf2 = context.getFlowScope().get("_csrf", CsrfToken.class);
        assertEquals(csrf1.getToken(), csrf2.getToken());
        assertEquals(csrf1.getParameterName(), csrf2.getParameterName());
        assertEquals(csrf1.getHeaderName(), csrf2.getHeaderName());
    }
}
