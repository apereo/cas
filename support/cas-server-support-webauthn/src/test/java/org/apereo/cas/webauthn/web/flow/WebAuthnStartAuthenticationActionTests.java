package org.apereo.cas.webauthn.web.flow;

import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link WebAuthnStartAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
@SpringBootTest(classes = BaseWebAuthnWebflowTests.SharedTestConfiguration.class)
public class WebAuthnStartAuthenticationActionTests {
    @Autowired
    @Qualifier("webAuthnStartAuthenticationAction")
    private Action webAuthnStartRegistrationAction;
}
