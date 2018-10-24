package org.apereo.cas;

import lombok.val;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.core.env.Environment;

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
@SpringBootTest(classes = {
    RefreshAutoConfiguration.class,
    AopAutoConfiguration.class
})
public class CasTomcatBannerTests {
    @Autowired
    private Environment environment;

    @Test
    public void verifyAction() {
        val banner = new CasTomcatBanner();
        val writer = new StringWriter();
        val out = new WriterOutputStream(writer, StandardCharsets.UTF_8);
        try (val stream = new PrintStream(out)) {
            banner.printBanner(environment, CasTomcatBanner.class, stream);
        }
        val output = writer.toString();
        assertNotNull(output);
    }
}
