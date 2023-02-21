package org.apereo.cas;

import lombok.val;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasJettyBannerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */

@Tag("WebApp")
public class CasJettyBannerTests {
    @Test
    public void verifyAction() {
        val banner = new CasJettyBanner();
        val writer = new StringWriter();
        val out = new WriterOutputStream(writer, StandardCharsets.UTF_8);
        val environment = new MockEnvironment();
        try (val stream = new PrintStream(out, true, StandardCharsets.UTF_8)) {
            banner.printBanner(environment, CasJettyBanner.class, stream);
        }
        val output = writer.toString();
        assertNotNull(output);
    }
}
