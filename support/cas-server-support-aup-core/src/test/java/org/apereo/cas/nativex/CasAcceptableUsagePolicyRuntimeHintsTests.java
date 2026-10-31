package org.apereo.cas.nativex;

import module java.base;
import org.apereo.cas.aup.TenantAcceptableUsagePolicyRepositoryBuilder;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasAcceptableUsagePolicyRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@Tag("Native")
class CasAcceptableUsagePolicyRuntimeHintsTests {
    @Test
    void verifyHints() throws Exception {
        val hints = new RuntimeHints();
        new CasAcceptableUsagePolicyRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(TenantAcceptableUsagePolicyRepositoryBuilder.class).test(hints));
    }
}
