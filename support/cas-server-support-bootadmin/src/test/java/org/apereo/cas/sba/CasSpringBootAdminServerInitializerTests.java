package org.apereo.cas.sba;

import org.apereo.cas.util.app.ApplicationUtils;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSpringBootAdminServerInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("WebApp")
class CasSpringBootAdminServerInitializerTests {
    @Test
    void verifyOperation() {
        val initializers = ApplicationUtils.getApplicationEntrypointInitializers();
        assertFalse(initializers.isEmpty());
        val initializer = (CasSpringBootAdminServerInitializer) initializers.getFirst();
        assertNotNull(initializer);
        assertFalse(initializer.getApplicationSources(ArrayUtils.EMPTY_STRING_ARRAY).isEmpty());
    }
}
