package org.apereo.cas.trusted.authentication.storage.fingerprint;

import lombok.NoArgsConstructor;
import org.apereo.cas.trusted.web.flow.fingerprint.ClientIpDeviceFingerprintComponentExtractor;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

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
        final var ex = new ClientIpDeviceFingerprintComponentExtractor();
        assertFalse(ex.extractComponent("casuser", new MockRequestContext(), false).isPresent());
    }

    @Test
    public void verifyClientIpFingerprintFound() {
        final var request = new MockHttpServletRequest();
        request.setRemoteAddr("1.2.3.4");
        final var clientInfo = new ClientInfo(request);
        ClientInfoHolder.setClientInfo(clientInfo);
        final var ex = new ClientIpDeviceFingerprintComponentExtractor();
        assertTrue(ex.extractComponent("casuser", new MockRequestContext(), false).isPresent());
    }
}
