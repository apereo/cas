package org.apereo.cas.webauthn.web.flow;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.execution.Action;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WebAuthnStartRegistrationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("WebflowActions")
public class WebAuthnStartRegistrationActionTests extends BaseWebAuthnWebflowTests {
    @Autowired
    @Qualifier("webAuthnStartRegistrationAction")
    private Action webAuthnStartRegistrationAction;

    @Test
    public void verifyOperation() {
        assertNotNull(webAuthnStartRegistrationAction);
    }

}
