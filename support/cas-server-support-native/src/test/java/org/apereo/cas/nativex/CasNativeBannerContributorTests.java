package org.apereo.cas.nativex;

import org.apereo.cas.config.CasNativeSupportConfiguration;
import org.apereo.cas.util.spring.boot.CasBanner;
import lombok.val;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.env.MockEnvironment;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasNativeBannerContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("Native")
@SpringBootTest(classes = CasNativeSupportConfiguration.class)
class CasNativeBannerContributorTests {

    @Test
    void verifyOperation() throws Throwable {
        System.setProperty("java.vendor.version", "Oracle GraalVM");
        val environment = new MockEnvironment();
        val banner = CasBanner.getInstance();
        try (val out = new ByteArrayOutputStream();
             val printStream = new PrintStream(out, true, StandardCharsets.UTF_8)) {
            banner.printBanner(environment, getClass(), printStream);
            val results = new String(out.toByteArray(), StandardCharsets.UTF_8);
            assertNotNull(results);
        }
    }
}
