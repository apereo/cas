package org.apereo.cas;

import org.apereo.cas.util.MockServletContext;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasConfigurationServerServletInitializerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
public class CasConfigurationServerServletInitializerTests {
    @BeforeAll
    public static void setup() {
        System.setProperty("spring.profiles.active", "native");
        System.setProperty("spring.cloud.config.server.native.searchLocations", "file://" + FileUtils.getTempDirectoryPath());
        System.setProperty("spring.cloud.config.server.git.uri", "file://" + FileUtils.getTempDirectoryPath());
    }

    @Test
    public void verifyInitializer() {
        val servletContext = new MockServletContext();
        val servletInitializer = new CasConfigurationServerServletInitializer();
        assertDoesNotThrow(new Executable() {
            @Override
            public void execute() throws Throwable {
                servletInitializer.onStartup(servletContext);
            }
        });
    }
}
