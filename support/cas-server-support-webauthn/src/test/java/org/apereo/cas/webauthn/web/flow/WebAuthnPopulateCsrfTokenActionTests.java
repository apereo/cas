package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.util.MockRequestContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
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
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class)
public class WebAuthnPopulateCsrfTokenActionTests {
    @Autowired
    @Qualifier("webAuthnPopulateCsrfTokenAction")
    private Action webAuthnPopulateCsrfTokenAction;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() throws Exception {
        val context = MockRequestContext.create(applicationContext);
        webAuthnPopulateCsrfTokenAction.execute(context);
        val csrf1 = context.getFlowScope().get("_csrf", CsrfToken.class);
        assertNotNull(csrf1);
        assertEquals("X-CSRF-TOKEN", csrf1.getHeaderName());

        webAuthnPopulateCsrfTokenAction.execute(context);
        val csrf2 = context.getFlowScope().get("_csrf", CsrfToken.class);
        assertEquals(csrf1, csrf2);
    }
}
