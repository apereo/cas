package org.apereo.cas;

import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * This is {@link CasEmbeddedContainerUtilsTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class CasEmbeddedContainerUtilsTests {

    @Test
    public void verifyRuntimeProperties() {
        final Map map = CasEmbeddedContainerUtils.getRuntimeProperties(true);
        assertEquals(1, map.size());
        assertTrue(map.containsKey(CasEmbeddedContainerUtils.EMBEDDED_CONTAINER_CONFIG_ACTIVE));
    }

    @Test
    public void verifyCasBanner() {
        final var banner = CasEmbeddedContainerUtils.getCasBannerInstance();
        assertNotNull(banner);
        final var out = new ByteArrayOutputStream();
        banner.printBanner(new MockEnvironment(), getClass(), new PrintStream(out));
        final var results = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertNotNull(results);
    }
}
