package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DelegatedClientsRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class DelegatedClientsRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new DelegatedClientsRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(DelegatedIdentityProviderFactory.class).test(hints));
    }
}
