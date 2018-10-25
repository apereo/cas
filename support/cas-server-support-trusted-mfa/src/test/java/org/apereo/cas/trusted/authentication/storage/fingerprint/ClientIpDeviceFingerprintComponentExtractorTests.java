package org.apereo.cas.trusted.authentication.storage.fingerprint;

import org.apereo.cas.trusted.web.flow.fingerprint.ClientIpDeviceFingerprintComponentExtractor;

import lombok.NoArgsConstructor;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ClientIpDeviceFingerprintComponentExtractorTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@NoArgsConstructor
public class ClientIpDeviceFingerprintComponentExtractorTests {

    @Test
    public void verifyClientIpFingerprintNotFound() {
        ClientInfoHolder.setClientInfo(null);
        val ex = new ClientIpDeviceFingerprintComponentExtractor();
        assertFalse(ex.extractComponent("casuser", new MockRequestContext(), false).isPresent());
    }

    @Test
    public void verifyClientIpFingerprintFound() {
        val request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        val clientInfo = new ClientInfo(request);
        ClientInfoHolder.setClientInfo(clientInfo);
        val ex = new ClientIpDeviceFingerprintComponentExtractor();
        assertTrue(ex.extractComponent("casuser", new MockRequestContext(), false).isPresent());
    }
}
