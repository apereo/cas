package org.apereo.cas.nativex;

import org.apereo.cas.acct.provision.AccountRegistrationProvisionerConfigurer;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasAccountManagementRuntimeHintsTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Native")
class CasAccountManagementRuntimeHintsTests {
    @Test
    void verifyHints() {
        val hints = new RuntimeHints();
        new CasAccountManagementRuntimeHints().registerHints(hints, getClass().getClassLoader());
        assertTrue(RuntimeHintsPredicates.reflection().onType(AccountRegistrationProvisionerConfigurer.class).test(hints));
        assertTrue(RuntimeHintsPredicates.proxies().forInterfaces(AccountRegistrationProvisionerConfigurer.class).test(hints));
    }
}
