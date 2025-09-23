package org.apereo.cas.nativex;

import org.apereo.cas.adaptors.x509.authentication.principal.X509SerialNumberPrincipalResolver;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link X509CertificateRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class X509CertificateRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new X509CertificateRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(X509SerialNumberPrincipalResolver.class).test(hints));
    }
}
