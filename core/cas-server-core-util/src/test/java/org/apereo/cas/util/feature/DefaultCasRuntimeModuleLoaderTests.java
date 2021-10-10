package org.apereo.cas.util.feature;

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
public class DefaultCasRuntimeModuleLoaderTests {
    @Test
    public void verifyOperation() {
        val loader = new DefaultCasRuntimeModuleLoader();
        assertDoesNotThrow(loader::load);
    }
}
