package org.apereo.cas;

import module java.base;
import org.apereo.cas.util.app.ApplicationUtils;
import org.apereo.cas.util.spring.boot.AbstractCasBanner;
import org.apereo.cas.util.spring.boot.CasBanner;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.core.env.Environment;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.core.metrics.jfr.FlightRecorderApplicationStartup;
import org.springframework.mock.env.MockEnvironment;
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
    @ClearSystemProperty(key = ApplicationUtils.SYSTEM_PROPERTY_APP_STARTUP)
    void verifyStartupIsNotInstrumentedByDefault() {
        assertSame(ApplicationStartup.DEFAULT, ApplicationUtils.getApplicationStartup());
    }

    @Test
    @SetSystemProperty(key = ApplicationUtils.SYSTEM_PROPERTY_APP_STARTUP, value = "unknown-strategy")
    void verifyUnknownStartupFallsBackToDefault() {
        assertSame(ApplicationStartup.DEFAULT, ApplicationUtils.getApplicationStartup());
    }

    @Test
    @SetSystemProperty(key = ApplicationUtils.SYSTEM_PROPERTY_APP_STARTUP, value = "buffering")
    void verifyBufferingStartupIsOptIn() {
        assertInstanceOf(BufferingApplicationStartup.class, ApplicationUtils.getApplicationStartup());
    }

    @Test
    @SetSystemProperty(key = ApplicationUtils.SYSTEM_PROPERTY_APP_STARTUP, value = "jfr")
    void verifyFlightRecorderStartupIsOptIn() {
        assertInstanceOf(FlightRecorderApplicationStartup.class, ApplicationUtils.getApplicationStartup());
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
