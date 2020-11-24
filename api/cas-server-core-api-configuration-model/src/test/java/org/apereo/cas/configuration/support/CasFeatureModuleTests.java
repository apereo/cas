package org.apereo.cas.configuration.support;

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
public class CasFeatureModuleTests {
    @Test
    public void verifyOperation() {
        val clazz = new CasFeatureModuleUnderTest();
        assertTrue(clazz.isDefined());
        clazz.setEnabled(false);
        assertFalse(clazz.isDefined());
    }

    @Getter
    @Setter
    private static class CasFeatureModuleUnderTest implements CasFeatureModule {
        @RequiredProperty
        private final String name = "value";

        @RequiredProperty
        private boolean enabled = true;
    }
}
