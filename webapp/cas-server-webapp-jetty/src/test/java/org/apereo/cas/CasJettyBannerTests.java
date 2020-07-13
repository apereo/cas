package org.apereo.cas;

import lombok.val;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.env.Environment;

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
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class
})
@Tag("WebApp")
public class CasJettyBannerTests {
    @Autowired
    private Environment environment;

    @Test
    public void verifyAction() {
        val banner = new CasJettyBanner();
        val writer = new StringWriter();
        val out = new WriterOutputStream(writer, StandardCharsets.UTF_8);
        try (val stream = new PrintStream(out, true, StandardCharsets.UTF_8)) {
            banner.printBanner(environment, CasJettyBanner.class, stream);
        }
        val output = writer.toString();
        assertNotNull(output);
    }
}
