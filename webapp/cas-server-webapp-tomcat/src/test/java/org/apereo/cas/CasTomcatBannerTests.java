package org.apereo.cas;

import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.PrintStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/**
 * This is {@link CasTomcatBannerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class
})
public class CasTomcatBannerTests {
    @Autowired
    private Environment environment;

    @Test
    public void verifyAction() {
        final var banner = new CasTomcatBanner();
        final var writer = new StringWriter();
        final var out = new WriterOutputStream(writer, StandardCharsets.UTF_8);
        try (var stream = new PrintStream(out)) {
            banner.printBanner(environment, CasTomcatBanner.class, stream);
        }
        final var output = writer.toString();
        assertNotNull(output);
    }
}
