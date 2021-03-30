package org.apereo.cas.web;

import org.apereo.cas.util.MockServletContext;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasWebApplicationServletInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@Tag("WebApp")
public class CasWebApplicationServletInitializerTests {

    static {
        System.setProperty("spring.main.allow-bean-definition-overriding", "true");
        System.setProperty("server.port", "8899");
    }

    @Test
    public void verifyInitializr() {
        val servletInitializer = new CasWebApplicationServletInitializer();
        val context = new MockServletContext();
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Exception {
                servletInitializer.onStartup(context);
            }
        });
    }
}
