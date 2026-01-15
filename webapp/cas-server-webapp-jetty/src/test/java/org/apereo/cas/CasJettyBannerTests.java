package org.apereo.cas;

import module java.base;
import lombok.val;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link CasJettyBannerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */

@Tag("WebApp")
class CasJettyBannerTests {
    @Test
    void verifyAction() throws Throwable {
        val banner = new CasJettyBanner();
        val writer = new StringWriter();
        val out = WriterOutputStream.builder().setWriter(writer).setCharset(StandardCharsets.UTF_8).get();
        val environment = new MockEnvironment();
        try (val stream = new PrintStream(out, true, StandardCharsets.UTF_8)) {
            banner.printBanner(environment, CasJettyBanner.class, stream);
        }
        val output = writer.toString();
        assertNotNull(output);
    }
}
