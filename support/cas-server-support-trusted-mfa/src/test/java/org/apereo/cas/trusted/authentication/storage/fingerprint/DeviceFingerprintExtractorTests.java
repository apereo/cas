package org.apereo.cas.trusted.authentication.storage.fingerprint;

import module java.base;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.trusted.web.flow.fingerprint.DeviceFingerprintExtractor;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.Ordered;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DeviceFingerprintExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("MFATrustedDevices")
class DeviceFingerprintExtractorTests {

    @Test
    void verifyOperation() throws Throwable {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        val noOp = DeviceFingerprintExtractor.noOp();
        assertEquals(Ordered.LOWEST_PRECEDENCE, noOp.getOrder());
        assertTrue(noOp.extract(RegisteredServiceTestUtils.getAuthentication(), request, response).isEmpty());
    }

}
