package org.apereo.cas;

import org.apereo.cas.util.MockServletContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSpringBootAdminServerServletInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("WebApp")
class CasSpringBootAdminServerServletInitializerTests {

    @Test
    void verifyInitializer() {
        System.setProperty("spring.cloud.compatibility-verifier.enabled", "false");
        
        val servletContext = new MockServletContext();
        val servletInitializer = new CasSpringBootAdminServletInitializer();
        assertDoesNotThrow(() -> servletInitializer.onStartup(servletContext));
    }
}
