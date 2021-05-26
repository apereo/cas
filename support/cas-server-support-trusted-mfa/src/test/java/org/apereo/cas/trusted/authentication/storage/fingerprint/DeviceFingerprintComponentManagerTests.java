package org.apereo.cas.trusted.authentication.storage.fingerprint;

import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintComponentManager;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DeviceFingerprintComponentManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
public class DeviceFingerprintComponentManagerTests {

    @Test
    public void verifyOperation() {
        val noOp = DeviceFingerprintComponentManager.noOp();
        assertEquals(Ordered.LOWEST_PRECEDENCE, noOp.getOrder());
        assertTrue(noOp.extractComponent("user", new MockRequestContext()).isEmpty());
    }

}
