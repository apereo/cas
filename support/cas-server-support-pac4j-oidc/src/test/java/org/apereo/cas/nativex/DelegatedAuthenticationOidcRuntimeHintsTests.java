package org.apereo.cas.nativex;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.oidc.profile.OidcProfile;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedAuthenticationOidcRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("Native")
class DelegatedAuthenticationOidcRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new DelegatedAuthenticationRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.serialization().onType(OidcProfile.class).test(hints));
    }
}
