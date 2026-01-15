package org.apereo.cas.support.saml.web.idp.web;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlIdPErrorControllerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("SAML2Web")
class SamlIdPErrorControllerTests {
    @Test
    void verifyOperation() {
        val ctrl = new SamlIdPErrorController();
        assertNotNull(ctrl.handleRequest());
    }
}
