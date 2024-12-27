package org.apereo.cas.nativex;

import org.apereo.cas.support.wsfederation.authentication.principal.WsFederationCredentialsToPrincipalResolver;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link WsFederationAuthenticationRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class WsFederationAuthenticationRuntimeHintsTests {

    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new WsFederationAuthenticationRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(WsFederationCredentialsToPrincipalResolver.class).test(hints));
    }
}
