package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.heimdall.authorizer.repository.AuthorizableResourceRepository;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link HeimdallRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Native")
class HeimdallRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new HeimdallRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(AuthorizableResourceRepository.class).test(hints));
    }
}
