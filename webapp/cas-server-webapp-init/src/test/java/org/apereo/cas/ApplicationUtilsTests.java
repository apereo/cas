package org.apereo.cas;

import org.apereo.cas.util.app.ApplicationUtils;
import org.apereo.cas.util.spring.boot.AbstractCasBanner;
import org.apereo.cas.util.spring.boot.CasBanner;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ApplicationUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Utility")
class ApplicationUtilsTests {
    @Test
    void verifyMainArgs() {
        assertFalse(ApplicationUtils.getApplicationEntrypointInitializers().isEmpty());
    }

    @Test
    void verifyCasBanner() {
        val banner = CasBanner.getInstance();
        assertNotNull(banner);
        val out = new ByteArrayOutputStream();
        banner.printBanner(new MockEnvironment(), getClass(), new PrintStream(out));
        val results = out.toString(StandardCharsets.UTF_8);
        assertNotNull(results);
    }

    @Test
    void verifyStartup() {
        assertNotNull(ApplicationUtils.getApplicationStartup());
        System.setProperty("CAS_APP_STARTUP", "buffering");
        assertNotNull(ApplicationUtils.getApplicationStartup());
        System.setProperty("CAS_APP_STARTUP", "jfr");
        assertNotNull(ApplicationUtils.getApplicationStartup());
    }

    @Test
    void verifyCustomBanner() {
        val banner = CasBanner.getInstance();
        assertNotNull(banner);
        val out = new ByteArrayOutputStream();
        banner.printBanner(new MockEnvironment(), getClass(), new PrintStream(out));
        val results = out.toString(StandardCharsets.UTF_8);
        assertNotNull(results);
        assertEquals("Custom", results);
    }

    public static class CustomBanner extends AbstractCasBanner {
        @Override
        public void printBanner(final @NonNull Environment environment, final Class<?> sourceClass, final @NonNull PrintStream out) {
            out.print(getTitle());
        }

        @Override
        public String getTitle() {
            return "Custom";
        }
    }
}
