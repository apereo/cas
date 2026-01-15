package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.MockServletContext;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasWebApplicationServletInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("ApacheTomcat")
@ExtendWith(CasTestExtension.class)
class CasWebApplicationServletInitializerTests {

    static {
        System.setProperty("server.port", "8899");
    }

    @Test
    void verifyInitializr() {
        val servletInitializer = new CasWebApplicationServletInitializer();
        val context = new MockServletContext();
        assertDoesNotThrow(() -> servletInitializer.onStartup(context));
    }
}
