package org.apereo.cas.nativex;

import org.apereo.cas.authentication.principal.resolvers.PersonDirectoryPrincipalResolver;
import org.apereo.cas.persondir.PersonDirectoryAttributeRepositoryPlanConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasPersonDirectoryRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
class CasPersonDirectoryRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasPersonDirectoryRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(PersonDirectoryAttributeRepositoryPlanConfigurer.class).test(hints));
        assertTrue(RuntimeHintsPredicates.reflection().onType(PersonDirectoryPrincipalResolver.class).test(hints));
    }
}
