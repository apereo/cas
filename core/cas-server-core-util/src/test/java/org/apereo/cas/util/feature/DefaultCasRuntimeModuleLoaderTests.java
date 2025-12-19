package org.apereo.cas.util.feature;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasRuntimeModuleLoaderTests}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Tag("Utility")
class DefaultCasRuntimeModuleLoaderTests {
    @Test
    void verifyOperation() {
        val loader = new DefaultCasRuntimeModuleLoader();
        assertDoesNotThrow(loader::load);
    }
}
