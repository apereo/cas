package org.apereo.cas.trusted.authentication.storage.fingerprint;

import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintComponentExtractor;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DeviceFingerprintComponentExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
public class DeviceFingerprintComponentExtractorTests {

    @Test
    public void verifyOperation() {
        val noOp = DeviceFingerprintComponentExtractor.noOp();
        assertEquals(Ordered.LOWEST_PRECEDENCE, noOp.getOrder());
        assertTrue(noOp.extractComponent("user", new MockRequestContext(), false).isEmpty());
    }

}
