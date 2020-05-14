package org.apereo.cas;

import org.apereo.cas.util.MockServletContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasSpringBootAdminServerServletInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class CasSpringBootAdminServerServletInitializerTests {

    @Test
    public void verifyInitializer() {
        val servletContext = new MockServletContext();
        val servletInitializer = new CasSpringBootAdminServletInitializer();
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                servletInitializer.onStartup(servletContext);
            }
        });
    }
}
