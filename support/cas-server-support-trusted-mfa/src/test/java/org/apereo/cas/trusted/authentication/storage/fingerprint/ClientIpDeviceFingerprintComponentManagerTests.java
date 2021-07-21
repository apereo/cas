package org.apereo.cas.trusted.authentication.storage.fingerprint;

import org.apereo.cas.trusted.web.flow.fingerprint.ClientIpDeviceFingerprintComponentManager;

import lombok.NoArgsConstructor;
import lombok.val;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link ClientIpDeviceFingerprintComponentManagerTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@NoArgsConstructor
@Tag("MFATrustedDevices")
public class ClientIpDeviceFingerprintComponentManagerTests {

    @Test
    public void verifyClientIpFingerprintNotFound() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        ClientInfoHolder.setClientInfo(null);
        val ex = new ClientIpDeviceFingerprintComponentManager();
        assertFalse(ex.extractComponent("casuser", request, response).isPresent());
    }

    @Test
    public void verifyClientIpFingerprintFound() {
        val request = new MockHttpServletRequest();
        val response = new MockHttpServletResponse();
        request.setRemoteAddr("1.2.3.4");
        val clientInfo = new ClientInfo(request);
        ClientInfoHolder.setClientInfo(clientInfo);
        val ex = new ClientIpDeviceFingerprintComponentManager();
        assertTrue(ex.extractComponent("casuser", request, response).isPresent());
    }
}
