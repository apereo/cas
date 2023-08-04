package org.apereo.cas.sba;

import org.apereo.cas.util.app.ApplicationUtils;
import lombok.val;
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
public class CasSpringBootAdminServerInitializerTests {
    @Test
    void verifyOperation() throws Exception {
        val initializers = ApplicationUtils.getApplicationEntrypointInitializers();
        assertFalse(initializers.isEmpty());
        val initializer = (CasSpringBootAdminServerInitializer) initializers.get(0);
        assertNotNull(initializer);
        assertFalse(initializer.getApplicationSources().isEmpty());
    }
}
