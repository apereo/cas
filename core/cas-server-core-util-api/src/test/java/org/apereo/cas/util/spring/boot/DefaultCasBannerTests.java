package org.apereo.cas.util.spring.boot;

import module java.base;
import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasBannerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DefaultCasBannerTests {
    @Test
    @Order(1)
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
    @Order(100)
    void verifyNoBanner() throws Throwable {
        System.setProperty("CAS_BANNER_SKIP", "true");
        val banner = new DefaultCasBanner();
        val environment = new MockEnvironment();
        try (val os = new ByteArrayOutputStream(); val out = new PrintStream(os)) {
            assertDoesNotThrow(() -> banner.printBanner(environment, this.getClass(), out));
            assertNotNull(os.toByteArray());
        }
    }
}
