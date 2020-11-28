package org.apereo.cas.util.spring.boot;

import lombok.val;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.function.Executable;
import org.springframework.mock.env.MockEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DefaultCasBannerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Tag("Simple")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DefaultCasBannerTests {
    @Test
    @Order(1)
    public void verifyOperation() throws Exception {
        val banner = new DefaultCasBanner();
        assertNotNull(banner.getTitle());
        assertNotNull(DefaultCasBanner.LINE_SEPARATOR);

        val environment = new MockEnvironment();
        try (val os = new ByteArrayOutputStream(); val out = new PrintStream(os)) {
            assertDoesNotThrow(new Executable() {
                @Override
                public void execute() {
                    banner.printBanner(environment, DefaultCasBannerTests.this.getClass(), out);
                }
            });
            assertNotNull(os.toByteArray());
        }
    }

    @Test
    @Order(100)
    public void verifyNoBanner() throws Exception {
        System.setProperty("CAS_BANNER_SKIP", "true");
        val banner = new DefaultCasBanner();
        val environment = new MockEnvironment();
        try (val os = new ByteArrayOutputStream(); val out = new PrintStream(os)) {
            assertDoesNotThrow(new Executable() {
                @Override
                public void execute() {
                    banner.printBanner(environment, DefaultCasBannerTests.this.getClass(), out);
                }
            });
            assertNotNull(os.toByteArray());
        }
    }
}
