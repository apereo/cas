package org.apereo.cas.util.spring.boot;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasBannerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
class DefaultCasBannerTests {
    @Test
    void verifyOperation() throws Throwable {
        val banner = new DefaultCasBanner();
        assertNotNull(banner.getTitle());
        assertNotNull(AbstractCasBanner.LINE_SEPARATOR);

        val environment = new MockEnvironment();
        try (val os = new ByteArrayOutputStream(); val out = new PrintStream(os)) {
            assertDoesNotThrow(() -> banner.printBanner(environment, this.getClass(), out));
            assertNotNull(os.toByteArray());
        }
    }

    @Test
    @SetSystemProperty(key = "CAS_BANNER_SKIP", value = "true")
    void verifyNoBanner() throws Throwable {
        val banner = new DefaultCasBanner();
        val environment = new MockEnvironment();
        try (val os = new ByteArrayOutputStream(); val out = new PrintStream(os)) {
            assertDoesNotThrow(() -> banner.printBanner(environment, this.getClass(), out));
            assertNotNull(os.toByteArray());
        }
    }
}
