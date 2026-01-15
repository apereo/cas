package org.apereo.cas.util.app;

import module java.base;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ApplicationEntrypointInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Simple")
class ApplicationEntrypointInitializerTests {
    @Test
    void verifyOperation() {
        ApplicationEntrypointInitializer.noOp().initialize(ArrayUtils.EMPTY_STRING_ARRAY);
        assertTrue(ApplicationEntrypointInitializer.noOp().getApplicationSources(ArrayUtils.EMPTY_STRING_ARRAY).isEmpty());
    }
}
