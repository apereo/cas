package org.apereo.cas;

import org.apereo.cas.util.MockServletContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEurekaServerServletInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class CasEurekaServerServletInitializerTests {

    @Test
    public void verifyInitializer() {
        val servletContext = new MockServletContext();
        val servletInitializer = new CasEurekaServerServletInitializer();
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                servletInitializer.onStartup(servletContext);
            }
        });
    }
}
