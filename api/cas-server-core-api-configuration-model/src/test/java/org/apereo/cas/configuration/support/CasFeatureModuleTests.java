package org.apereo.cas.configuration.support;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasFeatureModuleTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("CasConfiguration")
class CasFeatureModuleTests {
    @Test
    void verifyOperation() {
        val clazz = new CasFeatureModuleUnderTest();
        assertTrue(clazz.isDefined());
        clazz.setEnabled(false);
        assertFalse(clazz.isDefined());
        assertFalse(CasFeatureModule.baseline().isEmpty());
    }

    @Getter
    @Setter
    @SuppressWarnings("UnusedMethod")
    private static final class CasFeatureModuleUnderTest implements CasFeatureModule {
        @RequiredProperty
        private final String name = "value";

        @RequiredProperty
        private boolean enabled = true;
    }
}
