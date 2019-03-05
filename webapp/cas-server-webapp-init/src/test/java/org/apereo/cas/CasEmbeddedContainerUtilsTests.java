package org.apereo.cas;

import lombok.val;
import org.junit.jupiter.api.Test;
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
public class CasEmbeddedContainerUtilsTests {

    @Test
    public void verifyRuntimeProperties() {
        val map = CasEmbeddedContainerUtils.getRuntimeProperties(true);
        assertEquals(1, map.size());
        assertTrue(map.containsKey(CasEmbeddedContainerUtils.EMBEDDED_CONTAINER_CONFIG_ACTIVE));
    }

    @Test
    public void verifyCasBanner() {
        val banner = CasEmbeddedContainerUtils.getCasBannerInstance();
        assertNotNull(banner);
        val out = new ByteArrayOutputStream();
        banner.printBanner(new MockEnvironment(), getClass(), new PrintStream(out));
        val results = new String(out.toByteArray(), StandardCharsets.UTF_8);
        assertNotNull(results);
    }
}
