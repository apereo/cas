package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasEmbeddedContainerUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Tag("Simple")
public class CasEmbeddedContainerUtilsTests {

    @Test
    public void verifyCasBanner() {
        val banner = CasEmbeddedContainerUtils.getCasBannerInstance();
        assertNotNull(banner);
        val out = new ByteArrayOutputStream();
        banner.printBanner(new MockEnvironment(), getClass(), new PrintStream(out));
        val results = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertNotNull(results);
    }

    @Test
    public void verifyCustomBanner() {
        val banner = CasEmbeddedContainerUtils.getCasBannerInstance();
        assertNotNull(banner);
        val out = new ByteArrayOutputStream();
        banner.printBanner(new MockEnvironment(), getClass(), new PrintStream(out));
        val results = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertNotNull(results);
        assertEquals("Custom", results);
    }

    public static class CustomBanner extends AbstractCasBanner {
        @Override
        protected String getTitle() {
            return "Custom";
        }

        @Override
        public void printBanner(final Environment environment, final Class<?> sourceClass, final PrintStream out) {
            out.print(getTitle());
        }
    }
}
