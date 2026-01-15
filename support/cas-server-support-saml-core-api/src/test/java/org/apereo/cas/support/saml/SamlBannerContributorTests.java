package org.apereo.cas.support.saml;

import module java.base;
import org.apereo.cas.support.saml.banner.SamlBannerContributor;
import lombok.val;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link SamlBannerContributorTests}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Tag("WebApp")
class SamlBannerContributorTests {
    @Test
    void verifyAction() throws Throwable {
        val banner = new SamlBannerContributor();
        val writer = new StringWriter();
        val out = WriterOutputStream.builder().setWriter(writer).setCharset(StandardCharsets.UTF_8).get();
        val environment = new MockEnvironment();
        try (val stream = new PrintStream(out, true, StandardCharsets.UTF_8)) {
            banner.contribute(new Formatter(stream), environment);
        }
        val output = writer.toString();
        assertNotNull(output);
    }
}
