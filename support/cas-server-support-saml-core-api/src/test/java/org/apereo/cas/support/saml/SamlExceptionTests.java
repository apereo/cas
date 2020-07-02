package org.apereo.cas.support.saml;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlExceptionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("SAML")
public class SamlExceptionTests {
    @Test
    public void verifyArgs() {
        val ex = new SamlException("code", "message", List.of("arg1"));
        assertNotNull(ex.getMessage());
        assertNotNull(ex.getCode());
        assertNotNull(ex.getArgs());
    }

    @Test
    public void verifyArgsWithCause() {
        val ex = new SamlException("code", new RuntimeException(), List.of("arg1"));
        assertNotNull(ex.getMessage());
        assertNotNull(ex.getCode());
        assertNotNull(ex.getArgs());
    }
}
